package ru.kazov.colorpickerview.example;

import android.content.res.Resources;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import ru.kazov.colorpickerview.ColorPickerView;

public class MainActivity extends AppCompatActivity {

    Spinner spinner;
    ColorPickerView colorPickerView;

    LinearLayout colorContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colorContainer = findViewById(R.id.color_container);

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.harmonies, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch(i){
                    case 1:
                        colorPickerView.setHarmonyType(ColorPickerView.HarmonyTypes.COMPLEMENTARY);
                        break;
                    case 2:
                        colorPickerView.setHarmonyType(ColorPickerView.HarmonyTypes.SPLIT_COMPLEMENTARY);
                        break;
                    case 3:
                        colorPickerView.setHarmonyType(ColorPickerView.HarmonyTypes.ANALOGOUS);
                        break;
                    case 4:
                        colorPickerView.setHarmonyType(ColorPickerView.HarmonyTypes.ANALOGOUS_ACCENT);
                        break;
                    case 5:
                        colorPickerView.setHarmonyType(ColorPickerView.HarmonyTypes.TRIADIC);
                        break;
                    case 6:
                        colorPickerView.setHarmonyType(ColorPickerView.HarmonyTypes.SQUARE);
                        break;
                    case 7:
                        colorPickerView.setHarmonyType(ColorPickerView.HarmonyTypes.TETRADIC_PLUS);
                        break;
                    case 8:
                        colorPickerView.setHarmonyType(ColorPickerView.HarmonyTypes.TETRADIC_MINUS);
                        break;
                    case 9:
                        colorPickerView.setHarmonyType(ColorPickerView.HarmonyTypes.CLASH);
                        break;
                    case 10:
                        colorPickerView.setHarmonyType(ColorPickerView.HarmonyTypes.FIVE_TONE);
                        break;
                    case 11:
                        colorPickerView.setHarmonyType(ColorPickerView.HarmonyTypes.SIX_TONE);
                        break;
                    default:
                        colorPickerView.setHarmonyType(ColorPickerView.HarmonyTypes.NONE);

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        colorPickerView = findViewById(R.id.color_picker_view);
        colorPickerView.setColorListener(colorsList -> {

            colorContainer.removeAllViews();
            boolean isFirst = true;
            for (final int color : colorsList) {

                CardView cardView = new CardView(getApplicationContext());
                cardView.setRadius(convertDpToPixel(4));

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(convertDpToPixel(75), convertDpToPixel(75));
                if (!isFirst)
                    params.setMargins(convertDpToPixel(8), 0, 0, 0);

                cardView.setLayoutParams(params);
                cardView.setCardBackgroundColor(color);

                colorContainer.addView(cardView);
                cardView.invalidate();

                isFirst = false;
            }

        });

    }

    private int convertDpToPixel(float dp) {
        Resources resources = getApplicationContext().getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return (int) px;
    }
}