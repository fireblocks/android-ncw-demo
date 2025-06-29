# Fireblocks Android Demo

This demo application showcases both **Embedded Wallet (EW)** and **Non-Custodial Wallet (NCW)** SDKs from Fireblocks, demonstrating wallet creation, key management, transaction handling, and Web3 capabilities.

## SDK Overview

### Embedded Wallet (EW) SDK
The **Embedded Wallet SDK** is Fireblocks' latest offering that simplifies wallet integration by handling all wallet management logic internally. It uses the NCW SDK as its core library for cryptographic operations while eliminating the need for a custom backend infrastructure.

### Non-Custodial Wallet (NCW) SDK  
The **NCW SDK** provides the core cryptographic functionality for key generation and transaction signing. It serves as the foundation that powers the Embedded Wallet SDK.

### How They Work Together
- **EW SDK** is the primary SDK that uses **NCW SDK** as its core library
- **NCW SDK** handles key generation and transaction signing operations
- **EW SDK** manages all wallet operations, user flows, and backend communication
- This architecture eliminates the need for developers to build custom backend services

## Getting Started

### Recommended Setup (Default Configuration)
For the best developer experience and to understand the demo code examples, use the **`sandboxEmbeddedWalletDebug`** build variant:

```bash
./gradlew assembleSandboxEmbeddedWalletDebug
```

### Firebase Setup (Required)
1. **Create a Firebase Project**:
   - Go to the [Firebase Console](https://console.firebase.google.com/)
   - Create a new project or use an existing one

2. **Add Android App**:
   - Add an Android app to your Firebase project
   - Use package name: `com.fireblocks.ewdemo` (recommended for Embedded Wallet)

3. **Configure Authentication**:
   - Enable Google and Apple sign-in providers in Firebase Authentication
   - Configure OAuth settings for both providers

4. **Download Configuration**:
   - Download the `google-services.json` file
   - Place it in the `app/` folder of this project

### Transaction Updates Configuration (Embedded Wallet)

The Embedded Wallet implementation supports two methods for receiving transaction status updates:

#### Push Notifications (Default - Recommended)
- **Configuration**: `useTransactionPolling = false` in `FireblocksManager` (default)
- **Requirements**: 
  - Set up the Fireblocks minimal backend server (use our [EW Backend Demo](https://github.com/fireblocks/ew-backend-demo))
  - Create a webhook in the Fireblocks Console
  - Configure Firebase Cloud Messaging (FCM) for push notifications
- **Benefits**: Real-time updates, reduced battery usage, better user experience

#### Polling Mechanism (Fallback Option)
- **Configuration**: Set `useTransactionPolling = true` in `FireblocksManager`
- **Requirements**: No additional backend setup needed
- **Benefits**: Simpler setup, no external dependencies, works out of the box

**Note**: The demo uses push notifications (`useTransactionPolling = false`) by default for optimal performance. You can switch to polling (`useTransactionPolling = true`) if you prefer a simpler setup without backend requirements.

### Build Variants

The project uses a multi-dimensional flavor structure:

#### Server Environments:
- **`sandbox`** - Recommended for development and testing
- **`production`** - For production deployments  
- **`dev`** - Internal development (Fireblocks team only)

#### Wallet Types:
- **`embeddedWallet`** - Uses the EW SDK (recommended)
- **`ncw`** - Uses the NCW SDK directly

#### Example Build Variants:
- `sandboxEmbeddedWalletDebug` (recommended)
- `sandboxNcwDebug`
- `productionEmbeddedWalletRelease`
- `productionNcwRelease`

### Build and Run

1. **Clone the repository**
2. **Set up Firebase** (see Firebase Setup section above)
3. **Build the project**:
   ```bash
   ./gradlew assembleSandboxEmbeddedWalletDebug
   ```
4. **Install and run** on your device or emulator

## Documentation

For comprehensive documentation, setup guides, and API references:

📖 **[Fireblocks NCW Developer Guide](https://ncw-developers.fireblocks.com/v6.0/docs/getting-started)**

The developer guide includes:
- Detailed setup instructions
- API documentation (Javadoc JAR files available)
- Integration examples
- Best practices
- Troubleshooting guides

## Project Structure

```
app/src/
├── main/                    # Shared code
├── embeddedWallet/         # EW SDK specific implementations
├── ncw/                    # NCW SDK specific implementations
├── sandbox*/              # Sandbox environment configs
├── production*/           # Production environment configs
└── dev*/                 # Development environment configs
```

## Key Features Demonstrated

- 🔐 **Wallet Creation & Recovery** - Generate new wallets or recover existing ones
- 🔑 **Key Management** - Secure key generation, backup, and recovery
- 💸 **Transactions** - Send, receive, and track cryptocurrency transactions with real-time updates
- 🖼️ **NFT Support** - View, manage, and transfer NFTs across supported networks
- 🌐 **Web3 Integration** - Connect to dApps and sign Web3 transactions
- 📱 **Multi-Device** - Add and manage multiple devices per wallet
- 🔒 **Biometric Security** - Fingerprint and face unlock integration
- 🔔 **Push Notifications** - Real-time transaction status updates (EW implementation)
- 🎨 **Modern UI** - Built with Jetpack Compose

## Support

For technical support and questions:
- Review the [Developer Documentation](https://ncw-developers.fireblocks.com/v6.0/docs/getting-started)
- Check the code examples in this demo app
- Contact Fireblocks support for additional assistance