import 'package:external_app_launcher/external_app_launcher.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const ExampleApp());
}

class ExampleApp extends StatelessWidget {
  const ExampleApp({super.key});

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(home: LauncherPage());
  }
}

class LauncherPage extends StatefulWidget {
  const LauncherPage({super.key});

  @override
  State<LauncherPage> createState() => _LauncherPageState();
}

class _LauncherPageState extends State<LauncherPage> {
  final TextEditingController _urlController = TextEditingController(
    text: 'myapp://open?id=123',
  );
  bool _useChooser = false;
  String _status = '';

  @override
  void dispose() {
    _urlController.dispose();
    super.dispose();
  }

  Future<void> _canOpen() async {
    final url = _urlController.text;
    final canOpen = await ExternalAppLauncher.canOpen(url);
    setState(() {
      _status = canOpen ? '可以打开：$url' : '没有可处理该 URL 的应用：$url';
    });
  }

  Future<void> _open() async {
    final url = _urlController.text;

    try {
      await ExternalAppLauncher.open(
        url,
        useChooser: _useChooser,
        chooserTitle: '选择应用',
      );
      setState(() {
        _status = '已发起跳转：$url';
      });
    } on PlatformException catch (error) {
      setState(() {
        _status = '${error.code}: ${error.message ?? ''}';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('External App Launcher')),
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: <Widget>[
              TextField(
                controller: _urlController,
                decoration: const InputDecoration(
                  labelText: 'URL / Scheme',
                  hintText: 'myapp://open?id=123',
                  border: OutlineInputBorder(),
                ),
                keyboardType: TextInputType.url,
              ),
              const SizedBox(height: 12),
              SwitchListTile(
                contentPadding: EdgeInsets.zero,
                title: const Text('Use chooser'),
                value: _useChooser,
                onChanged: (value) {
                  setState(() {
                    _useChooser = value;
                  });
                },
              ),
              const SizedBox(height: 12),
              FilledButton(onPressed: _canOpen, child: const Text('Can Open')),
              const SizedBox(height: 8),
              FilledButton.tonal(onPressed: _open, child: const Text('Open')),
              const SizedBox(height: 16),
              Text(_status),
            ],
          ),
        ),
      ),
    );
  }
}
