package fmg.android.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DemoActivity extends Activity implements View.OnClickListener {

   private DemoView demoView;
   private Button changeColorBtn;

   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.demo_view);

      demoView = (DemoView)findViewById(R.id.demo_view);
      changeColorBtn = (Button)findViewById(R.id.change_color);
      changeColorBtn.setOnClickListener(this);

   }

   @Override
   public void onClick(View view) {
      demoView.onNextImages();
   }

}
