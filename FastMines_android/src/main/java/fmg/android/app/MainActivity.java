package fmg.android.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import fmg.core.types.ClickCellResult;

public class MainActivity extends AppCompatActivity {

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      Intent intent = new Intent(this, CustomViewActivity.class);
      startActivity(intent);
   }

}
