package com.example.SocialMediaIntegration.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.SocialMediaIntegration.R;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
//import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;

public class HomeActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = HomeActivity.class.getSimpleName();
    private LoginButton loginButton;
    private CircleImageView circleImageView;
    private TextView txtName, txtEmail;
    private CallbackManager callbackManager;
    private SignInButton SignIn;
    private TextView Name, Email;
    private ImageView Prof_Pic;

    //Twitter local variables//
    private TwitterLoginButton twitterLoginButton;
    private ImageView userProfileImageView;
    private TextView userDetailsLabel;
    private TwitterAuthClient client;
    //Google local variables//
    private GoogleApiClient googleApiClient;
    private static final int REQ_CODE = 9001;
    private static final int RC_TWITTER = 9002;
    //private static final int RC_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))//enable logging when app is in debug mode
                .twitterAuthConfig(new TwitterAuthConfig(getResources().getString(R.string.twitter_consumer_key), getResources().getString(R.string.twitter_CONSUMER_SECRET)))
                .debug(true)//enable debug mode
                .build();
        Twitter.initialize(config);
        setContentView(R.layout.activity_home);


        userProfileImageView = findViewById(R.id.user_profile_image_view);
        userDetailsLabel = findViewById(R.id.user_details_label);

        //google variables
        SignIn = (SignInButton) findViewById(R.id.sign_in_button);
        Name = (TextView) findViewById(R.id.profile_name1);
        Email = (TextView) findViewById(R.id.profile_email1);
        Prof_Pic = (ImageView) findViewById(R.id.profile_pic1);


        //facebook variables
        loginButton = findViewById(R.id.login_button);
        txtName = findViewById(R.id.profile_name);
        txtEmail = findViewById(R.id.profile_email);
        circleImageView = findViewById(R.id.profile_pic);


        twitterLoginButton = findViewById( R.id.default_twitter_login_button);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {

                // Do something with result, which provides a TwitterSession for making API calls
                TwitterSession twitterSession = result.data;

                //call fetch email only when permission is granted
                fetchTwitterEmail(twitterSession);
                TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
                Call<User> call = twitterApiClient.getAccountService().verifyCredentials(true, false, true);
                call.enqueue(new Callback<User>() {
                    @Override
                    public void success(Result<User> result) {
                        User user = result.data;
                        userDetailsLabel.setText("User Id : " + user.id + "\nUser Name : " + user.name + "\nEmail Id : " + user.email + "\nScreen Name : " + user.screenName);

                        //String imageProfileUrl = user.profileImageUrl;
                        //  Log.e(TAG, "Data : " + imageProfileUrl);

                        //  imageProfileUrl = imageProfileUrl.replace("_normal", "");

                        ///load image using Picasso
                        //Picasso.with(HomeActivity.this)
                        //      .load(imageProfileUrl)
                        //     .placeholder(R.mipmap.ic_launcher_round)
                        //     .into(userProfileImageView);

                    }
                    @Override
                    public void failure(TwitterException exception) {
                        Toast.makeText(HomeActivity.this, "Failed to authenticate. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
                Toast.makeText(HomeActivity.this, "Failed to authenticate. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });



        // Google Sigin
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();


        final GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this,
                        this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();


        SignIn.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick (View v){
                if (v == loginButton) {
                    loginButton.performClick();
                } else if (v == SignIn) {
                    loginButton.setVisibility(View.GONE);
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                    startActivityForResult(signInIntent, REQ_CODE);
                    twitterLoginButton.setVisibility(View.GONE);

                } else if (v == twitterLoginButton) {
                    twitterLoginButton.performClick();
                }

            }
        });



        callbackManager = CallbackManager.Factory.create();
        loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));
        checkLoginStatus();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }


    //facebook Access token//
    AccessTokenTracker tokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if (currentAccessToken == null) {
                txtName.setText("");
                txtEmail.setText("");

                circleImageView.setImageResource(0);
                Toast.makeText(HomeActivity.this, "User Logged out", Toast.LENGTH_LONG).show();
                SignIn.setVisibility(View.VISIBLE);
                twitterLoginButton.setVisibility(View.VISIBLE);

            } else {
                SignIn.setVisibility(View.GONE);
                twitterLoginButton.setVisibility(View.GONE);
                loadUserProfile(currentAccessToken);
            }

        }
    };

    private void loadUserProfile(AccessToken newAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String first_name = object.getString("first_name");
                    String last_name = object.getString("last_name");
                    String email = object.getString("email");
                    String id = object.getString("id");

                    String image_url = "https://graph.facebook.com/" + id + "/picture?type=normal";

                    txtEmail.setText(email);
                    txtName.setText(first_name + " " + last_name);


                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.dontAnimate();

                    Glide.with(HomeActivity.this).load(image_url).into(circleImageView);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ;
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", " first_name, last_name, email, id");
        request.setParameters(parameters);
        request.executeAsync();


    }

    private void checkLoginStatus() {
        if (AccessToken.getCurrentAccessToken() != null) {
            loadUserProfile(AccessToken.getCurrentAccessToken());
        }
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQ_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleResult(result);
        } else if (requestCode == RC_TWITTER) {
            twitterLoginButton.onActivityResult(requestCode, resultCode, data);
        }else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private void updateUI(Boolean isSignedIn) {

        if (isSignedIn) {
            SignIn.setVisibility(View.GONE);

        } else {
            SignIn.setVisibility(View.VISIBLE);
        }
    }

    private void handleResult(GoogleSignInResult result) {

        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            String name = account.getDisplayName();
            String email = account.getEmail();
            String img_url = account.getPhotoUrl().toString();
            Name.setText(name);
            Email.setText(email);
            Glide.with(this).load(img_url).into(Prof_Pic);
            updateUI(true);
        } else {
            Toast.makeText(HomeActivity.this, "User logged Out", Toast.LENGTH_SHORT).show();
            updateUI(false);
        }

    }
    public void fetchTwitterEmail(final TwitterSession twitterSession) {
        client.requestEmail(twitterSession, new Callback<String>() {
            @Override
            public void success(Result<String> result) {
                //here it will give u only email and rest of other information u can get from TwitterSession
                userDetailsLabel.setText("User Id : " + twitterSession.getUserId() + "\nScreen Name : " + twitterSession.getUserName() + "\nEmail Id : " + result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(HomeActivity.this, "Failed to authenticate. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public TwitterSession getTwitterSession() {
        TwitterSession session = TwitterCore.getInstance().getSessionManager().getActiveSession();

        //NOTE : if you want to get token and secret too use uncomment the below code
        TwitterAuthToken authToken = session.getAuthToken();
        String token = authToken.token;
        String secret = authToken.secret;

        return session;
    }
}
