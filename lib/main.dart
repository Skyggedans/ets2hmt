import 'package:ets2hmt/dashboard.dart';
import 'package:ets2hmt/settings.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';

final RouteObserver<PageRoute> routeObserver = RouteObserver<PageRoute>();

void main() async {
  final prefs = await SharedPreferences.getInstance();
  final GlobalKey<NavigatorState> navigatorKey =
      new GlobalKey<NavigatorState>();

  final channel = MethodChannel('skyggedans.com/ets2hmt')
    ..setMethodCallHandler((call) async {
      if (call.method == 'onSpeechEvent') {
        final command = call.arguments['command'];

        switch (command) {
          case 'show settings':
            {
              navigatorKey.currentState.pushNamed('/settings');
            }
        }
      }
    });

  runApp(Ets2HmtApp(prefs, channel, navigatorKey));
}

class Ets2HmtApp extends StatelessWidget {
  final SharedPreferences prefs;
  final MethodChannel channel;
  final GlobalKey<NavigatorState> navigatorKey;

  Ets2HmtApp(this.prefs, this.channel, this.navigatorKey) : super();

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'RealWear HMT-1 controller for ETS2/ATS',
      theme: ThemeData(
        brightness: Brightness.dark,
        accentColor: Colors.deepOrange,
        buttonColor: Colors.deepOrange,
        textTheme: TextTheme(
          body1: TextStyle(fontSize: 18.0),
          body2: TextStyle(fontSize: 18.0),
          subhead: TextStyle(fontSize: 18.0),
          button: TextStyle(fontSize: 18.0),
          caption: TextStyle(fontSize: 18.0),
        ),
      ),
      darkTheme: ThemeData(
        brightness: Brightness.dark,
      ),
      home: prefs.getKeys().isNotEmpty
          ? Dashboard(prefs, channel, routeObserver)
          : Settings(prefs, channel, routeObserver),
      routes: <String, WidgetBuilder>{
        '/dashboard': (BuildContext context) =>
            Dashboard(prefs, channel, routeObserver),
        '/settings': (BuildContext context) =>
            Settings(prefs, channel, routeObserver),
      },
      navigatorObservers: [routeObserver],
      navigatorKey: navigatorKey,
    );
  }
}
