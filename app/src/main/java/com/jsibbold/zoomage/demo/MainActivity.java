/**
 * Copyright 2016 Jeffrey Sibbold
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jsibbold.zoomage.demo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.jsibbold.zoomage.ZoomageView;

import com.jsibbold.zoomage.demo.R;

public class MainActivity extends AppCompatActivity {

    private ZoomageView demoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.jsibbold.zoomage.demo.R.layout.activity_main);
        demoView = (ZoomageView)findViewById(R.id.demoView);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.zoom_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.zoomable:
                demoView.setZoomable(!demoView.zoomable());
                break;
            case R.id.translatable:
                demoView.setTranslatable(!demoView.translatable());
                break;
            case R.id.restrictBounds:
                demoView.setRestrictBounds(!demoView.restrictBounds());
                break;
            case R.id.animateReset:
                demoView.setAnimateReset(!demoView.animateReset());
                break;
            case R.id.reset:
                demoView.reset();
                break;
            case R.id.autoReset:
                showResetOptions();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showResetOptions() {
        CharSequence[] options = new CharSequence[]{"Under", "Over", "Over and Under", "None"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                demoView.setAutoReset(which);
            }
        });

        builder.create().show();
    }
}
