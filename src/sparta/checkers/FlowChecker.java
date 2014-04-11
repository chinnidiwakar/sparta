package sparta.checkers;

/*>>>
import org.checkerframework.checker.compilermsgs.qual.CompilerMessageKey;
*/


import org.checkerframework.common.basetype.BaseTypeChecker;
import org.checkerframework.common.basetype.BaseTypeVisitor;
import org.checkerframework.framework.qual.PolyAll;
import org.checkerframework.framework.qual.StubFiles;
import org.checkerframework.framework.qual.TypeQualifiers;
import org.checkerframework.framework.source.SupportedLintOptions;
import org.checkerframework.framework.stub.StubGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import sparta.checkers.quals.PolySink;
import sparta.checkers.quals.PolySource;
import sparta.checkers.quals.Sink;
import sparta.checkers.quals.Source;
import sparta.checkers.quals.FineSink;
import sparta.checkers.quals.FineSource;


@TypeQualifiers({ Source.class, Sink.class, PolySource.class, PolySink.class, PolyAll.class, FineSource.class, FineSink.class })
@StubFiles("information_flow.astub")
@SupportedOptions({ FlowPolicy.POLICY_FILE_OPTION, FlowChecker.MSG_FILTER_OPTION,
        FlowChecker.IGNORE_NOT_REVIEWED, FlowVisitor.CHECK_CONDITIONALS_OPTION })
@SupportedLintOptions({ FlowPolicy.STRICT_CONDITIONALS_OPTION })

public class FlowChecker extends BaseTypeChecker {
    public static final String SPARTA_OUTPUT_DIR = System.getProperty("user.dir")+File.separator+"sparta-out"+File.separator;
    public static final String MSG_FILTER_OPTION = "msgFilter";
    public static final String IGNORE_NOT_REVIEWED = "ignorenr";

    protected Set<String> unfilteredMessages;
    // Methods that are not in a stub file
    protected final Map<String, Map<String, Map<Element, Integer>>> notInStubFile;

    public FlowChecker() {
        super();
        this.notInStubFile = new HashMap<>();
    }
    @Override
    protected BaseTypeVisitor<?> createSourceVisitor() {
        return new FlowVisitor(this);
    }

    @Override
    public void initChecker() {
        super.initChecker();

        String unfilteredStr = getOption(MSG_FILTER_OPTION);
        if (unfilteredStr == null) {
            unfilteredMessages = null;
        } else {
            final String[] unfilteredMsgs = unfilteredStr.split(":");
            unfilteredMessages = new HashSet<String>();
            for (final String unfilteredMsg : unfilteredMsgs) {
                if (!unfilteredMsg.trim().isEmpty()) {
                    unfilteredMessages.add(unfilteredMsg.trim());
                }
            }

            if (unfilteredMessages.isEmpty()) {
                unfilteredMessages = null;
            }
        }
    }
    @Override
public void typeProcessingOver() {
        File outputDir = new File(SPARTA_OUTPUT_DIR);
        if(!outputDir.exists()){
            outputDir.mkdir();
        }
        if (outputDir.exists() && outputDir.isDirectory()) {
            printMethods();
            FlowAnnotatedTypeFactory factory = ((FlowVisitor) visitor).getTypeFactory();
            factory.flowAnalizer.printImpliedFlowsVerbose();
            factory.flowAnalizer.printImpliedFlowsForbidden();
            factory.flowAnalizer.printAllFlows();
            factory.flowAnalizer.printIntentFlowsByComponent();
        }
    }

    // TODO: would be nice if you could pass a file name
    private final String printMissMethod = SPARTA_OUTPUT_DIR+"missingAPI.astub";
    // TODO: would be nice if there was a command line argument to turn this on
    // and off
    private final boolean printFrequency = true;

    private void printMethods() {
        if (notInStubFile.isEmpty())
            return;
       
        int methodCount = 0;
        try( PrintStream out = new PrintStream(new File(printMissMethod))) {
            for (String pack : notInStubFile.keySet()) {
                out.println("package " + pack + ";");
                for (String clss : notInStubFile.get(pack).keySet()) {
                    out.println("class " + clss + "{");
                    Map<Element, Integer> map = notInStubFile.get(pack).get(clss);
                    for (Element element : map.keySet()) {
                        StubGenerator stubGen = new StubGenerator(out);
                        if (printFrequency)
                            out.println("    //" + map.get(element) + " (" + element.getSimpleName()
                                    + ")");
                        stubGen.skeletonFromMethod(element);
                        methodCount++;
                    }
                    out.println("}");
                }
            }
            System.err.println(methodCount + " methods to annotate.");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
       
    }

    @Override
    public void message(Diagnostic.Kind kind, Object source, /*@CompilerMessageKey*/
            String msgKey, Object... args) {
        if (unfilteredMessages == null || unfilteredMessages.contains(msgKey)) {
            super.message(kind, source, msgKey, args);
        }
    }
}
