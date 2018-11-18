package com.helper.helper.view.category;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.helper.helper.R;
import com.helper.helper.controller.HttpManager;
import com.helper.helper.controller.LoaderImageTask;
import com.helper.helper.interfaces.HttpCallback;
import com.helper.helper.model.LED;
import com.helper.helper.model.LEDCategory;
import com.helper.helper.view.widget.LEDCardView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CategoryActivity extends AppCompatActivity
{
    private final static String TAG = CategoryActivity.class.getSimpleName()+"/DEV";
    /******************* Define widgtes in view *******************/
    private GridLayout m_categoryLEDGrid;
    /**************************************************************/
    private Activity m_thisActivity;
    private LEDCategory m_thisCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        m_thisActivity = this;
        m_thisCategory = new LEDCategory(
                getIntent().getStringExtra("name"),
                getIntent().getStringExtra("bkgColor"),
                getIntent().getStringExtra("notice"),
                getIntent().getStringExtra("character")
        );

        /******************* Connect widgtes with layout *******************/
        ImageView backImg = findViewById(R.id.backCategory);
        m_categoryLEDGrid = findViewById(R.id.categoryLEDGrid);
        ConstraintLayout categoryParent = findViewById(R.id.parentConstraint);
        TextView categoryName = findViewById(R.id.categoryName);
        TextView categoryNotice = findViewById(R.id.categoryNotice);
        ImageView categoryChar = findViewById(R.id.categoryChar);
        /*******************************************************************/


        /******************* Make Listener in View *******************/
        backImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        /*************************************************************/

        categoryParent.setBackgroundColor(Color.parseColor(m_thisCategory.getBkgColor()));
        categoryName.setText(m_thisCategory.getName());
        categoryNotice.setText(m_thisCategory.getNotice());

        String url = getString(R.string.server_uri)
                .concat("/")
                .concat(getString(R.string.led_resource_uri))
                .concat(m_thisCategory.getCharacter())
                .concat(getString(R.string.gif_format));

        /** load image by url **/

        LoaderImageTask loader = new LoaderImageTask(categoryChar);
        loader.execute(url);

        addLEDintoCategoryGrid();

    }

    private void addLEDintoCategoryGrid() {
        //1/ get List of category
        if( HttpManager.useCollection(getString(R.string.collection_led)) ) {
            try {
                final JSONObject reqObject = new JSONObject();
                reqObject.put("category", m_thisCategory.getName());

                HttpManager.requestHttp(reqObject, "", "GET", "", new HttpCallback() {
                    @Override
                    public void onSuccess(JSONArray jsonArray) throws JSONException {
                        //2. Add LED into GridView
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            final LED led = new LED(
                                    new LED.Builder()
                                            .index(jsonObject.getString("index"))
                                            .name(jsonObject.getString("name"))
                                            .creator(jsonObject.getString("creator"))
                                            .downloadCnt(jsonObject.getInt("downloadcnt"))
                                            .type(jsonObject.getString("type"))
                            );

                            LEDCardView cardViewLED = new LEDCardView(m_thisActivity);
                            cardViewLED.setCardNameText(led.getName());

                            String url = getString(R.string.server_uri)
                                    .concat("/")
                                    .concat(getString(R.string.led_resource_uri))
                                    .concat(led.getIndex())
                                    .concat(getString(R.string.gif_format));

                            cardViewLED.setCardImageView(url);

                            final LEDCardView targetCardView = cardViewLED;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    targetCardView.setOnClickCustomDialogEnable(LEDCardView.DOWNLOAD_DIALOG_TYPE, led, m_thisActivity);
                                    m_categoryLEDGrid.addView(targetCardView);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(String err) { }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
