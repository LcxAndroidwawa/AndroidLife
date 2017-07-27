package com.camnter.smartrouter.core;

import android.app.Activity;
import android.support.annotation.NonNull;
import java.util.Map;

/**
 * @author CaMnter
 */

public interface Router {

    void register(@NonNull final Map<String, Class<? extends Activity>> routerMapping);

    void setFieldValue(@NonNull final Activity activity);

}