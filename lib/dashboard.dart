import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_inappwebview/flutter_inappwebview.dart';
import 'package:shared_preferences/shared_preferences.dart';

class Dashboard extends StatefulWidget {
  final SharedPreferences prefs;
  final MethodChannel channel;
  final RouteObserver routeObserver;

  Dashboard(this.prefs, this.channel, this.routeObserver) : super();

  @override
  State createState() => _DashboardState();
}

class _DashboardState extends State<Dashboard> with RouteAware {
  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
    widget.routeObserver.subscribe(this, ModalRoute.of(context));
  }

  @override
  void dispose() {
    widget.routeObserver.unsubscribe(this);
    super.dispose();
  }

  @override
  void didPush() {
    widget.channel.invokeMethod('start', {
      'targetIp': widget.prefs.getString('targetIp'),
      'openTrackPort': widget.prefs.getInt('openTrackPort'),
      'voiceControlPort': widget.prefs.getInt('voiceControlPort'),
      'deviceIndex': widget.prefs.getInt('deviceIndex'),
      'sampleRate': widget.prefs.getInt('sampleRate'),
      'showOrientation': widget.prefs.getBool('sendOrientation'),
      'sendRawData': widget.prefs.getBool('sendRawData')
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      floatingActionButton: FloatingActionButton(
        child: Icon(Icons.settings),
        onPressed: () {
          Navigator.pushNamed(context, '/settings');
        },
      ),
      body: InAppWebView(
        initialUrl: widget.prefs.getString('dashStudioUrl'),
        initialHeaders: {},
        initialOptions: InAppWebViewWidgetOptions(),
      ),
    );
  }
}
