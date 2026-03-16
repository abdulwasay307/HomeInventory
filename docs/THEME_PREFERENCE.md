# Theme preference storage

Theme (dark/light) is stored **per user** so each logged-in user sees their own choice.

## Where it is stored

- **API**: Java [Preferences](https://docs.oracle.com/en/java/javase/17/docs/api/java.prefs/java/util/preferences/Preferences.html) (user node for `ThemeManager`’s package).
- **Keys**: `theme_guest` for the login screen (before any user logs in), and `theme_<email>` for each user (e.g. `theme_user@example.com`).
- **Values**: `"dark"` or `"light"`.

The exact file/location is decided by the JVM. Typical examples:

- **macOS**: under the user’s `Library/Preferences` (e.g. Java prefs for the app).
- **Windows**: in the registry under the current user.
- **Linux**: under `~/.java/.userPrefs/` or similar.

So the preference is **local to this machine** and this OS user, not stored on the server. Each app user (by email) has a separate key, so when they log in again on the same machine, their last theme choice is restored.

## When it is updated

- **Login screen**: Uses `theme_guest`. Changing theme there updates `theme_guest`.
- **After login**: Uses `theme_<your email>`. Changing theme updates that key.
- **Logout**: Next time you see the login screen, `theme_guest` is used again.

To have theme follow the user across devices, you’d need to store it in your backend (e.g. user profile/settings API) and load/save it when the user logs in or toggles theme.
