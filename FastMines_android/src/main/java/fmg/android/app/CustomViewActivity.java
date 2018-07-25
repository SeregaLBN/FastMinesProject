package fmg.android.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CustomViewActivity extends Activity implements View.OnClickListener {

   private CustomView customView;
   private Button changeColorBtn;

   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.custom_view);

      customView = (CustomView)findViewById(R.id.custom_view);
      changeColorBtn = (Button)findViewById(R.id.change_color);
      changeColorBtn.setOnClickListener(this);

   }

   @Override
   public void onClick(View view) {
      customView.changeColor();
   }

}
