package ru.bokus.w1.Activity;

import android.app.Application;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.annotation.ReportsCrashes;

@ReportsCrashes(formKey = "",formUri = "http://37.252.0.113/kassa/")
public class MyApplication extends Application {
  @Override
  public void onCreate() {
    // The following line triggers the initialization of ACRA
    super.onCreate();
    ACRAConfiguration acraConfiguration = ACRA.getNewDefaultConfig(this);    
    acraConfiguration.setFormUri("http://37.252.0.113/kassa/index.php");
    ACRA.setConfig(acraConfiguration);
    ACRA.init(this);
  }
}	
