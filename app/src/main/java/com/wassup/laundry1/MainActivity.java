package com.wassup.laundry1;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button otpLogin;
    private static final int FRAMEWORK_REQUEST_CODE = 1;
    private String initialStateParam;
    private int selectedThemeId = -1;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        otpLogin = (Button) findViewById(R.id.mobileLogin);
        otpLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // Checking Permissions
                    String permission = android.Manifest.permission.READ_PHONE_STATE;
                    if ((checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.READ_PHONE_STATE, android.Manifest.permission.RECEIVE_SMS, android.Manifest.permission.GET_ACCOUNTS}, 1);
                    } else {
                        // Already Permission given
                        onLogin(LoginType.PHONE);
                    }
                } else {
                    onLogin(LoginType.PHONE);
                }
            }
        });
    }

    // Login With Mobile

    private void onLogin(final LoginType loginType) {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        final AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder
                = createAccountKitConfiguration(loginType);
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
        startActivityForResult(intent, FRAMEWORK_REQUEST_CODE);
    }

    private AccountKitConfiguration.AccountKitConfigurationBuilder createAccountKitConfiguration(
            final LoginType loginType) {
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder
                = new AccountKitConfiguration.AccountKitConfigurationBuilder(
                loginType,
                getResponseType());
        initialStateParam = UUID.randomUUID().toString();
        configurationBuilder.setInitialAuthState(initialStateParam);
        configurationBuilder.setFacebookNotificationsEnabled(true);
        selectedThemeId = R.style.AppLoginTheme_Login;
        configurationBuilder.setTheme(selectedThemeId);
        final String[] blackList = getResources().getStringArray(R.array.blacklistedSmsCountryCodes);
        final String[] whiteList = getResources().getStringArray(R.array.whitelistedSmsCountryCodes);
        configurationBuilder.setSMSBlacklist(blackList);
        configurationBuilder.setSMSWhitelist(whiteList);
        configurationBuilder.setReadPhoneStateEnabled(true);
        configurationBuilder.setReceiveSMS(true);

        return configurationBuilder;
    }

    private AccountKitActivity.ResponseType getResponseType() {
        return AccountKitActivity.ResponseType.TOKEN;
        //return AccountKitActivity.ResponseType.CODE;
    }

    public void getAccountDetails() {
        AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
            @Override
            public void onSuccess(final Account account) {
                phone = account.getPhoneNumber().toString();
                if (phone.contains("+")) {
                    phone = phone.substring(3);
                }
                Toast.makeText(getApplicationContext(),"Verified Phone Number:"+phone,Toast.LENGTH_LONG).show();
                System.out.println("Verified Phone Number:"+phone);

            }

            @Override
            public void onError(final AccountKitError error) {
                System.out.println("Number:::" + error.toString());
            }
        });
    }

    // Requesting Permission
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onLogin(LoginType.PHONE);
                } else {
                    Toast.makeText(getApplication(), "Permission required", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(final int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FRAMEWORK_REQUEST_CODE) {
            try {
                final AccountKitLoginResult loginResult =
                        data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
                if (loginResult.getError() != null) {
                    Toast.makeText(getBaseContext(), "Error Occured, Please try again", Toast.LENGTH_LONG).show();
                } else if (loginResult.wasCancelled()) {
                    Toast.makeText(getBaseContext(), "Login Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    final com.facebook.accountkit.AccessToken accessToken = loginResult.getAccessToken();
                    final String authorizationCode = loginResult.getAuthorizationCode();
                    final long tokenRefreshIntervalInSeconds =
                            loginResult.getTokenRefreshIntervalInSeconds();
                    if (accessToken != null) {
                        getAccountDetails();

                    } else if (authorizationCode != null) {
                        getAccountDetails();
                    } else {
                        Toast.makeText(getBaseContext(), "Error Occured, Please try again", Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Toast.makeText(getBaseContext(), "Error Occured, Please try again", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}
