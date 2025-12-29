# Security Policy

## Supported Versions

We release patches for security vulnerabilities. Which versions are eligible for receiving such patches depends on the CVSS v3.0 Rating:

| Version | Supported          |
| ------- | ------------------ |
| Latest  | :white_check_mark: |
| < Latest| :x:                |

## Reporting a Vulnerability

If you discover a security vulnerability in Path, please report it responsibly:

1. **Do not** open a public issue
2. Email security details to: [Your email or security contact]
3. Include:
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

We will acknowledge receipt of your report within 48 hours and provide an update on the status of the vulnerability within 7 days.

## Security Considerations

Path is designed with security in mind:

- **Local-Only Storage**: All data is stored locally on the device
- **No Network Authentication**: No accounts means no authentication vulnerabilities
- **Optional Network Features**: AI features connect only to user-provided servers
- **No Third-Party SDKs**: Minimal external dependencies reduce attack surface
- **Standard Android Security**: Follows Android security best practices

## Known Security Considerations

### AI Integration

If you enable AI features and connect to an Ollama server:
- Ensure your Ollama server is properly secured
- Use HTTPS if accessing over the network (not just localhost)
- Be aware that verse text is sent to your server
- Path does not validate or secure the connection to your server

### Local Data Storage

- Data is stored in Android's standard app data directory
- Data is protected by Android's app sandbox
- Uninstalling the app deletes all data
- No encryption is applied to local data (feature may be added in future)

### Network Requests

- Bible API requests are made over HTTPS to bible-api.com
- No authentication tokens or personal data are transmitted
- Only book/chapter/verse references are sent

## Security Best Practices for Users

1. **Keep Android Updated**: Ensure your device runs the latest Android security patches
2. **Review Permissions**: Path only requests necessary permissions (INTERNET for optional features)
3. **Secure Your Ollama Server**: If using AI features, secure your self-hosted server
4. **Regular Backups**: Export your notes regularly if you want to preserve them

## Disclosure Policy

We follow responsible disclosure practices:

1. Security issues are addressed promptly
2. Patches are released as soon as possible
3. Users are notified of security updates through release notes
4. Credit is given to security researchers who responsibly report vulnerabilities

---

**Note**: Since Path stores all data locally and doesn't require accounts, the attack surface is minimal. However, we take security seriously and appreciate responsible disclosure of any vulnerabilities.

