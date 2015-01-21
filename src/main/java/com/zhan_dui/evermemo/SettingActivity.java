package com.zhan_dui.evermemo;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.evernote.client.android.EvernoteSession;
import com.evernote.edam.type.User;
import com.umeng.analytics.MobclickAgent;
import com.zhan_dui.sync.Evernote;
import com.zhan_dui.sync.Evernote.EvernoteLoginCallback;

import me.pcqpcq.android.jokermemo.R;

public class SettingActivity extends ActionBarActivity implements
		OnClickListener, EvernoteLoginCallback, OnCheckedChangeListener {
	private ViewGroup mBindEvernote;

	public static final String OPEN_MEMO_WHEN_START_UP = "OPEN_MEMO_WHEN_START_UP";

	private Evernote mEvernote;
	private TextView mBindText;
	private ToggleButton mToggleButton;
	private Context mContext;
	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.in_push_right_to_left,
				R.anim.in_stable);
		mContext = this;
		mSharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(mContext);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activtiy_setting);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setTitle(getString(R.string.setting));
		getSupportActionBar().setDisplayUseLogoEnabled(false);
		mBindEvernote = (ViewGroup) findViewById(R.id.bind_evernote);
		mBindText = (TextView) findViewById(R.id.bind_text);
		mToggleButton = (ToggleButton) findViewById(R.id.open_toggle);
		mBindEvernote.setOnClickListener(this);
		mEvernote = new Evernote(mContext, this);
		if (mEvernote.isLogin()) {
			bindSuccess();
		}
		mToggleButton.setOnCheckedChangeListener(this);
		mToggleButton.setChecked(mSharedPreferences.getBoolean(
				OPEN_MEMO_WHEN_START_UP, false));
		findViewById(R.id.rate).setOnClickListener(this);
		findViewById(R.id.setting_start).setOnClickListener(this);
	}

	private void bindSuccess() {
		findViewById(R.id.bind_arrow).setVisibility(View.INVISIBLE);
		if (mEvernote.getUsername() == null) {
			mBindText.setText(R.string.unbind_evernote_username_null);
			mEvernote.getUserInfo();
		} else {
			mBindText.setText(getString(R.string.unbind_evernote,
					mEvernote.getUsername()));
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			finish();
			overridePendingTransition(R.anim.in_stable,
					R.anim.out_push_left_to_right);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bind_evernote:
			if (!mEvernote.isLogin()) {
				mEvernote.auth();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setMessage(R.string.unbind_tips)
						.setTitle(R.string.unbind_title)
						.setPositiveButton(R.string.unbind_sure,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										mEvernote.Logout();
									}
								})
						.setNegativeButton(R.string.unbind_cancel,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								}).create().show();
			}
			break;
		case R.id.rate:
			Uri uri = Uri.parse("market://details?id="
					+ mContext.getPackageName());
			Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
			try {
				startActivity(goToMarket);
			} catch (ActivityNotFoundException e) {
				Toast.makeText(mContext, R.string.can_not_open_market,
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.setting_start:
			mToggleButton.performClick();
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case EvernoteSession.REQUEST_CODE_OAUTH:
			mEvernote.onAuthFinish(resultCode);
			break;
		}
	}

	public interface LoginCallback {
		public void onAuthFinish(int resultCode);
	}

	@Override
	public void onLoginResult(Boolean result) {
		if (result) {
			bindSuccess();
			Toast.makeText(mContext, R.string.bind_success, Toast.LENGTH_SHORT)
					.show();
		} else {
			Toast.makeText(mContext, R.string.bind_failed, Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onUserinfo(Boolean result, User user) {
		mBindText.setText(getString(R.string.unbind_evernote,
				user.getUsername()));
	}

	@Override
	public void onLogout(Boolean reuslt) {
		if (reuslt) {
			Toast.makeText(mContext, R.string.unbind_success,
					Toast.LENGTH_SHORT).show();
			findViewById(R.id.bind_arrow).setVisibility(View.VISIBLE);
			mBindText.setText(R.string.bind_evernote);
		} else {
			Toast.makeText(mContext, R.string.unbind_failed, Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			MobclickAgent.onEvent(mContext, "open_quick_launch");
		} else {
			MobclickAgent.onEvent(mContext, "close_quick_launch");
		}
		mSharedPreferences.edit()
				.putBoolean(OPEN_MEMO_WHEN_START_UP, isChecked).commit();
	}

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
