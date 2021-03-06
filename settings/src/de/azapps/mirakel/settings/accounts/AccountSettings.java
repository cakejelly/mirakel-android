package de.azapps.mirakel.settings.accounts;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import de.azapps.mirakel.DefinitionsHelper;
import de.azapps.mirakel.DefinitionsHelper.NoSuchListException;
import de.azapps.mirakel.helper.MirakelCommonPreferences;
import de.azapps.mirakel.helper.MirakelModelPreferences;
import de.azapps.mirakel.helper.MirakelPreferences;
import de.azapps.mirakel.model.account.AccountMirakel;
import de.azapps.mirakel.model.account.AccountMirakel.ACCOUNT_TYPES;
import de.azapps.mirakel.settings.R;
import de.azapps.mirakel.sync.SyncAdapter;
import de.azapps.tools.Log;

public class AccountSettings implements OnPreferenceChangeListener {

	protected static final String	TAG	= "AccountSettings";
	private AccountMirakel			account;
	private boolean					v4_0;
	private Object					settings;
	private Context					ctx;

	@SuppressLint("NewApi")
	public AccountSettings(AccountSettingsFragment accountSettingsFragment, AccountMirakel account) {
		this.account = account;
		this.v4_0 = true;
		this.settings = accountSettingsFragment;
		this.ctx = accountSettingsFragment.getActivity();
	}

	public AccountSettings(AccountSettingsActivity accountSettingsFragment, AccountMirakel account) {
		this.account = account;
		this.v4_0 = false;
		this.settings = accountSettingsFragment;
		this.ctx = accountSettingsFragment;
	}

	public void setup() throws NoSuchListException {
		if (this.account == null) {
			throw new NoSuchListException();
		}
		AccountManager am = AccountManager.get(this.ctx);
		final Account a = this.account.getAndroidAccount();
		final Preference syncUsername = findPreference("syncUsername");
		if (syncUsername != null) {
			syncUsername.setEnabled(false);
			syncUsername.setSummary(a == null ? this.ctx
					.getString(R.string.local_account) : a.name);
		}
		final Preference syncServer = findPreference("syncServer");
		if (syncServer != null) {
			syncServer.setEnabled(false);
			if (a != null && a.type.equals(AccountMirakel.ACCOUNT_TYPE_MIRAKEL)) syncServer
					.setSummary(am
							.getUserData(a, SyncAdapter.BUNDLE_SERVER_URL));
			else syncServer.setSummary("");
		}
		final CheckBoxPreference syncUse = (CheckBoxPreference) findPreference("syncUse");
		if (syncUse != null) {
			syncUse.setChecked(this.account.isEnabeld());
			syncUse.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					AccountSettings.this.account.setEnabeld((Boolean) newValue);
					AccountSettings.this.account.save();
					return true;
				}
			});
		}
		if (this.account.getType() == ACCOUNT_TYPES.LOCAL) {
			removePreference("syncUse");
			removePreference("syncServer");
			removePreference("syncUsername");
		}
		Preference syneType = findPreference("sync_type");
		if (syneType != null) {
			syneType.setSummary(this.account.getType().typeName(this.ctx));
		}

		final CheckBoxPreference defaultAccount = (CheckBoxPreference) findPreference("defaultAccount");
		if (defaultAccount != null) {
			defaultAccount.setChecked(MirakelModelPreferences.getDefaultAccount()
					.getId() == this.account.getId());
			defaultAccount
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							if ((Boolean) newValue) {
								MirakelModelPreferences.setDefaultAccount(AccountSettings.this.account);
							} else {
								MirakelModelPreferences
										.setDefaultAccount(AccountMirakel
												.getLocal());
							}
							defaultAccount.setChecked((Boolean) newValue);
							return false;
						}
					});
		}

		final Preference syncPassword = findPreference("syncPassword");
		if (syncPassword != null) {
			syncPassword.setEnabled(false);
		}

		final Preference syncInterval = findPreference("syncFrequency");
		if (syncInterval != null) {
			syncInterval.setEnabled(false);

			// final ListPreference syncInterval = (ListPreference) findPreference("syncFrequency");
			if (MirakelModelPreferences.getSyncFrequency(this.account) == -1) {
				syncInterval.setSummary(R.string.sync_frequency_summary_man);
			} else {
				syncInterval.setSummary(this.ctx.getString(
						R.string.sync_frequency_summary,
						MirakelModelPreferences.getSyncFrequency(this.account)));
			}
			syncInterval
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(Preference preference, Object newValue) {
							Log.e(TAG, "" + newValue.toString());
							final Bundle bundle = new Bundle();
							long longVal = Long.parseLong(newValue.toString());
							if (longVal == -1) {
								syncInterval
										.setSummary(R.string.sync_frequency_summary_man);
							} else {
								syncInterval.setSummary(AccountSettings.this.ctx.getString(
										R.string.sync_frequency_summary,
										longVal));
							}
							if (AccountSettings.this.account != null) {
								ContentResolver.removePeriodicSync(a,
										DefinitionsHelper.AUTHORITY_TYP, bundle);
								if (longVal != -1) {
									ContentResolver.setSyncAutomatically(a,
											DefinitionsHelper.AUTHORITY_TYP, true);
									ContentResolver.setIsSyncable(a,
											DefinitionsHelper.AUTHORITY_TYP, 1);
									// ContentResolver.setMasterSyncAutomatically(true);
									ContentResolver.addPeriodicSync(a,
											DefinitionsHelper.AUTHORITY_TYP, bundle,
											longVal * 60);
								}
							} else {
								Log.d(TAG, "account does not exsist");
							}
							return true;
						}
					});
			if (this.account.getType() != ACCOUNT_TYPES.TASKWARRIOR) {// we can control this only for tw
				removePreference("syncFrequency");
			} else {
				syncInterval.setEnabled(true);
			}
		}

	}


	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO: Nothing? Then why does this class implement
		// OnPreferenceChangeListener?
		return false;
	}

	// TODO: This repeats in SpecialListSettings. Maybe extract into a
	// superclass?
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private Preference findPreference(String key) {
		if (this.v4_0) {
			return ((AccountSettingsFragment) this.settings).findPreference(key);
		}
		return ((AccountSettingsActivity) this.settings).findPreference(key);
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void removePreference(String which) {
		Preference pref = findPreference(which);
		if (pref != null) {
			if (this.v4_0) {
				((AccountSettingsFragment) this.settings).getPreferenceScreen()
						.removePreference(pref);
			} else {
				((AccountSettingsActivity) this.settings).getPreferenceScreen()
						.removePreference(pref);
			}
		}
	}

}
