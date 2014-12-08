package io.branch.testbed;

import io.branch.referral.Branch;
import io.branch.referral.Branch.BranchReferralInitListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class ReferralCodeActivity extends Activity {
	Branch branch;
	
	EditText txtReferralCode;
	EditText txtAmount;
	EditText txtPrefix;
	EditText txtExpiration;
	RadioGroup radioType;
	RadioGroup radioLocation;
	Button cmdGetReferralCode;
	Button cmdValidateReferralCode;
	Button cmdRedeemReferralCode;
	TextView txtValid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_referral_code);

		txtReferralCode = (EditText) findViewById(R.id.editReferralCode);
		txtAmount = (EditText) findViewById(R.id.editAmount);
		txtPrefix = (EditText) findViewById(R.id.editPrefix);
		txtExpiration = (EditText) findViewById(R.id.editExpiration);
		radioType = (RadioGroup) findViewById(R.id.codeType);
		radioLocation = (RadioGroup) findViewById(R.id.codeLocation);
		txtValid = (TextView) findViewById(R.id.txtValid);
		cmdGetReferralCode = (Button) findViewById(R.id.cmdGetReferralCode);
		cmdValidateReferralCode = (Button) findViewById(R.id.cmdValidateReferralCode);
		cmdRedeemReferralCode = (Button) findViewById(R.id.cmdRedeemReferralCode);
		
		cmdGetReferralCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String prefix = txtPrefix.getText().toString();
				int amount;
				try {
					amount = Integer.parseInt(txtAmount.getText().toString());
				} catch (Exception e) {
					txtAmount.setText(null);
					txtAmount.setHint("Invalid value");
					return;
				}
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date expiration = null;
				if (txtExpiration.getText().toString().length() > 0) {
					try {  
					    expiration = format.parse(txtExpiration.getText().toString());  
					} catch (ParseException e) {
						txtExpiration.setText(null);
					    txtExpiration.setHint("Invalid date format");
					    return;
					}
				}
				
				branch.getReferralCode(prefix, amount, expiration, null, getCalculationType(), getLocation(), new BranchReferralInitListener() {
					@Override
					public void onInitFinished(JSONObject referralCode) {
						try {
							// Ugly! will add error code soon.
							if (!referralCode.has("error_message")) {
								txtReferralCode.setText(referralCode.getString("referral_code"));
							} else {
								txtReferralCode.setText(referralCode.getString("error_message"));
							}
						} catch (JSONException e) {
							txtReferralCode.setText("Error parsing JSON");
						}
					}
				});
			}
		});
		
		cmdValidateReferralCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				txtValid.setVisibility(View.GONE);
				
				final String referral_code = txtReferralCode.getText().toString();
				if (referral_code.length() > 0) {
					branch.validateReferralCode(referral_code, new BranchReferralInitListener() {
						@Override
						public void onInitFinished(JSONObject referralCode) {
							try {
								txtValid.setVisibility(View.VISIBLE);
								if (!referralCode.has("error_message")) {
									
									String code = referralCode.getString("referral_code");
									if (referral_code.equals(code)) {
										txtValid.setText("Valid");
									} else {
										txtValid.setText("Mismatch!");
									}
								} else {
									txtValid.setText("Invalid");
								}
							} catch (JSONException e) {
								txtReferralCode.setText("Error parsing JSON");
							}
						}
					});
				}
			}
		});
		
		cmdRedeemReferralCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				txtValid.setVisibility(View.GONE);

				final String referral_code = txtReferralCode.getText().toString();
				if (referral_code.length() > 0) {
					branch.applyReferralCode(referral_code, new BranchReferralInitListener() {
						@Override
						public void onInitFinished(JSONObject referralCode) {
							try {
								txtValid.setVisibility(View.VISIBLE);
								if (!referralCode.has("error_message")) {
									
									String code = referralCode.getString("referral_code");
									if (referral_code.equals(code)) {
										txtValid.setText("Applied");
									} else {
										txtValid.setText("Mismatch!");
									}
								} else {
									txtValid.setText("Invalid");
								}
							} catch (JSONException e) {
								txtReferralCode.setText("Error parsing JSON");
							}
						}
					});
				}
			}
		});
	}
	
	public int getCalculationType() {
		switch(radioType.getCheckedRadioButtonId()) {
			case R.id.type0:
				return 0;
			case R.id.type1:
				return 1;
			default:
				return 0;
		}
	}
	
	public int getLocation() {
		switch(radioLocation.getCheckedRadioButtonId()) {
			case R.id.location0:
				return 0;
			case R.id.location2:
				return 2;
			case R.id.location3:
				return 3;
			default:
				return 0;
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		branch = Branch.getInstance(this.getApplicationContext());
		branch.initSession();
	}


	@Override
	protected void onStop() {
		super.onStop();
		branch.closeSession();
	}

}
