package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import sparta.checkers.quals.*;
import static sparta.checkers.quals.FlowPermission.*;

public class DeviceAdminAdd extends Activity {

	@Override
        public @IntentMap(value={@Extra(key="android.app.extra.DEVICE_ADMIN",source={ANY},
                sink={BIND_DEVICE_ADMIN}),
                @Extra(key="android.app.extra.ADD_EXPLANATION", source={ANY},
                        sink={BIND_DEVICE_ADMIN})
        }) Intent getIntent() {
            return super.getIntent();
        }
	
	@Override
	@ReceiveIntent("startActivity,1")
	public void setIntent(@IntentMap(value={@Extra(key="android.app.extra.DEVICE_ADMIN",source={ANY},
	            sink={BIND_DEVICE_ADMIN}),
	            @Extra(key="android.app.extra.ADD_EXPLANATION", source={ANY},
	                    sink={BIND_DEVICE_ADMIN})
	    })Intent newIntent) {
	    super.setIntent(newIntent);
	}

}