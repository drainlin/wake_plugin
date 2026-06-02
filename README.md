# external_app_launcher

A Flutter Android plugin for opening external apps through URL schemes, deeplinks, and web links with `Intent.ACTION_VIEW`.

The Android implementation adds:

- `Intent.CATEGORY_BROWSABLE`
- `Intent.CATEGORY_DEFAULT`
- `Intent.FLAG_ACTIVITY_NEW_TASK`

This can reduce the chance that the target Activity is opened inside the current Flutter app task stack. Android still cannot guarantee this 100% if the target app has unusual `taskAffinity`, `launchMode`, or task configuration.

## Usage

```dart
import 'package:external_app_launcher/external_app_launcher.dart';

final canOpen = await ExternalAppLauncher.canOpen('myapp://open?id=123');

if (canOpen) {
  await ExternalAppLauncher.open(
    'myapp://open?id=123',
    useChooser: true,
    chooserTitle: '选择应用',
  );
}
```

If no installed app can handle the URL, `open` throws a `PlatformException` with code `NO_ACTIVITY`.
