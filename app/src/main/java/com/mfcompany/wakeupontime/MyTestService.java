package com.mfcompany.wakeupontime;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.Nullable;

/**
 * Created by MIGUEL ANGEL GOMEZ CASAS on 26/11/2020.
 */

public class MyTestService extends IntentService {

    public MyTestService() {
        super("MyTestService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i("MyTestService", "Servicio ejecutandose. Recordatorios");
    }
}
