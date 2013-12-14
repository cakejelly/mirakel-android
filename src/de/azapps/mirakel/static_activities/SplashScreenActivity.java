/*******************************************************************************
 * Mirakel is an Android App for managing your ToDo-Lists
 * 
 * Copyright (c) 2013 Anatolij Zelenin, Georg Semmler.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.azapps.mirakel.static_activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import de.azapps.mirakel.helper.MirakelPreferences;
import de.azapps.mirakel.main_activity.MainActivity;
import de.azapps.mirakel.model.list.ListMirakel;
import de.azapps.mirakel.model.list.SpecialList;
import de.azapps.mirakelandroid.R;

public class SplashScreenActivity extends Activity {
	public static final String EXIT = "de.azapps.mirakel.EXIT";

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getIntent().getAction() == EXIT) {
			finish();
			return;
		}

		// Setup splashscreen
		boolean darkTheme = MirakelPreferences.isDark();
		if (!darkTheme)
			setTheme(R.style.Theme_SplashScreen);

		// Intents
		if (MirakelPreferences.isStartupAllLists()) {
			Intent intent = new Intent(SplashScreenActivity.this,
					MainActivity.class);
			intent.setAction(MainActivity.SHOW_LISTS);
			startActivityForResult(intent, 42);
		} else {
			ListMirakel sl = SpecialList.firstSpecial();
			if (sl == null) {
				sl = SpecialList.newSpecialList(getString(R.string.list_all),
						" done=0 ", true, this);
			}
			int listId = MirakelPreferences.getStartupList().getId();
			Intent intent = new Intent(SplashScreenActivity.this,
					MainActivity.class);
			intent.setAction(MainActivity.SHOW_LIST);
			intent.putExtra(MainActivity.EXTRA_ID, listId);
			finish();
			startActivityForResult(intent, 1);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash_screen, menu);
		return true;
	}
}
