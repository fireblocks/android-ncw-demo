<div align="center">
  <img src="./image/logo.svg" width="400px" heigth="200px">
  <h1>NCW Android Demo Application</h1>
  <br/>
  <h3> Resources: </h3>
  <div>
    <a href="https://ncw-developers.fireblocks.com/docs">Fireblocks NCW Developer documentation</a>
  </div>
  <div>
    <a href="https://developers.fireblocks.com/">Fireblocks Developer Portal</a>
  </div>
  <div>
    <a href="https://www.fireblocks.com/developer-sandbox-sign-up/">Fireblocks Sandbox Sign Up</a>
  </div>
</div>
<br/>
<div align="center" style="border-top: 1px solid #4e5259;">
  <h2> 📖 Intro</h2>
</div>

The Fireblocks Non-Custodial Wallet (NCW) solution allows you to securely and effectively manage digital assets by granting end users full control over their funds or tokens without reliance on a third-party custodian. /
The Fireblocks NCW comes with native web and mobile Software Development Kits (SDKs), which businesses can seamlessly integrate into their existing applications. This integration provides a safeguarded and smooth method for storing and overseeing digital assets. /

This repository contains the NCW Android demo application that can be used for reference during development.

<br/>

<div align="center" style="border-top: 1px solid #4e5259;">
  <h2> 🛠️ Configuration & Setup</h2>
</div>

### Prerequisites:
- Backend server (see customer backend documentation [here](https://ncw-developers.fireblocks.com/docs/backend-server-configuration))
- Firebase project for Google and Apple authentication
- Apple developer account for Apple authentication

### Setup:

1. Clone this repo: `git clone https://github.com/fireblocks/android-ncw-demo.git`
2. Create a Firebase project for user authentication (guide here)
3. Download the `google_services.json` file and place it in the `app` directory
4. The application can run in production or sandbox environments that are set in `/app/src/production/envs` (fireblocks_data.json). Sandbox is the default one. 
5. Update the `host` value in the relevant `fireblocks_data.json` file with your backend server URL.

<div align="center" style="border-top: 1px solid #4e5259;">
  <h2> 🔥 Running the application</h2>
</div>

1. For the first build go to `build variants` in Android Studio and make sure that `productionDebug` is selected.
2. Run the application
