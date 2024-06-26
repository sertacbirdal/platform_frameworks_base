/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.widget.listview;

import android.os.Bundle;
import android.widget.ListScenario;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ListWithHeaders extends ListScenario {
    @Override
    protected void init(Params params) {
        params.setStackFromBottom(false)
                .setStartingSelectionPosition(-1)
                .setNumItems(24)
                .setItemScreenSizeFactor(0.14);
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        final ListView listView = getListView();
        listView.setItemsCanFocus(true);

        for (int i = 0; i < 12; i++) {
            Button header = new Button(this);
            header.setText("Header View");
            listView.addHeaderView(header);
        }

        for (int i = 0; i < 12; i++) {
            Button footer = new Button(this);
            footer.setText("Footer View");
            listView.addFooterView(footer);
        }

        final ListAdapter adapter = listView.getAdapter();
        listView.setAdapter(adapter);
    }
}
