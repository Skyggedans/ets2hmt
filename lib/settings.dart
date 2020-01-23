import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:shared_preferences/shared_preferences.dart';

class Settings extends StatefulWidget {
  final SharedPreferences prefs;
  final MethodChannel channel;
  final RouteObserver routeObserver;

  Settings(this.prefs, this.channel, this.routeObserver) : super();

  @override
  State createState() => _SettingsState();
}

class _SettingsState extends State<Settings> with RouteAware {
  final _formKey = GlobalKey<FormState>();

  String _targetIp;
  int _openTrackPort;
  int _voiceControlPort;
  String _dashStudioUrl;
  int _deviceIndex;
  int _sampleRate;
  bool _sendOrientation;
  bool _sendRawData;

  @override
  void initState() {
    _targetIp = widget.prefs.getString('targetIp') ?? '192.168.1.1';
    _openTrackPort = widget.prefs.getInt('openTrackPort') ?? 5555;
    _voiceControlPort = widget.prefs.getInt('voiceControlPort') ?? 2587;
    _dashStudioUrl = widget.prefs.getString('dashStudioUrl') ??
        'http://192.168.1.1:8888/Dash#Euro Truck Simulator 2';
    _deviceIndex = widget.prefs.getInt('deviceIndex') ?? 0;
    _sampleRate = widget.prefs.getInt('sampleRate') ?? 0;
    _sendOrientation = widget.prefs.getBool('sendOrientation') ?? true;
    _sendRawData = widget.prefs.getBool('sendRawData') ?? true;

    super.initState();
  }

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
    widget.channel.invokeMethod('stop');
  }

  @override
  void didPop() {
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
      appBar: AppBar(
        title: const Text('RealWear HMT-1 controller for ETS2/ATS'),
        actions: <Widget>[
          FlatButton(
            child: Row(
              children: <Widget>[
                const Icon(Icons.save),
                const Text('Save Settings'),
              ],
            ),
            onPressed: () {
              if (_formKey.currentState.validate()) {
                _formKey.currentState.save();

                widget.prefs.setString('targetIp', _targetIp);
                widget.prefs.setInt('openTrackPort', _openTrackPort);
                widget.prefs.setInt('voiceControlPort', _voiceControlPort);
                widget.prefs.setString('dashStudioUrl', _dashStudioUrl);
                widget.prefs.setInt('deviceIndex', _deviceIndex);
                widget.prefs.setInt('sampleRate', _sampleRate);
                widget.prefs.setBool('sendOrientation', _sendOrientation);
                widget.prefs.setBool('sendRawData', _sendRawData);

                Navigator.pop(context);
              }
            },
          ),
        ],
      ),
      body: Padding(
        padding: EdgeInsets.all(20.0),
        child: Form(
          key: _formKey,
          child: Column(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            mainAxisSize: MainAxisSize.values[1],
            children: <Widget>[
              Row(
                children: <Widget>[
                  Expanded(
                    flex: 2,
                    child: TextFormField(
                      autovalidate: true,
                      initialValue: _targetIp,
                      decoration: InputDecoration(
                        labelText: 'Target IP',
                      ),
                      onSaved: (value) {
                        setState(() {
                          _targetIp = value;
                        });
                      },
                    ),
                  ),
                  SizedBox(width: 20),
                  Expanded(
                    flex: 1,
                    child: TextFormField(
                      initialValue: _openTrackPort.toString(),
                      decoration: InputDecoration(
                        labelText: 'OpenTrack Port',
                      ),
                      keyboardType:
                          TextInputType.numberWithOptions(decimal: false),
                      onSaved: (value) {
                        setState(() {
                          _openTrackPort = num.tryParse(value);
                        });
                      },
                    ),
                  ),
                  SizedBox(width: 20),
                  Expanded(
                    flex: 1,
                    child: TextFormField(
                      initialValue: _voiceControlPort.toString(),
                      decoration: InputDecoration(
                        labelText: 'VoiceControl Port',
                      ),
                      keyboardType:
                          TextInputType.numberWithOptions(decimal: false),
                      onSaved: (value) {
                        setState(() {
                          _voiceControlPort = num.tryParse(value);
                        });
                      },
                    ),
                  ),
                ],
              ),
              TextFormField(
                initialValue: _dashStudioUrl,
                decoration: InputDecoration(
                  labelText: 'Dash Studio URL',
                ),
                onSaved: (value) {
                  setState(() {
                    _dashStudioUrl = value;
                  });
                },
              ),
              Row(
                children: <Widget>[
                  Expanded(
                    flex: 1,
                    child: DropdownButtonFormField(
                      value: _deviceIndex,
                      decoration: InputDecoration(labelText: 'Device Index'),
                      items: Iterable<int>.generate(16)
                          .toList()
                          .map(
                            (index) => DropdownMenuItem<int>(
                              value: index,
                              child: SizedBox(
                                width: 200,
                                child: Text(index.toString()),
                              ),
                            ),
                          )
                          .toList(),
                      onChanged: (value) {
                        setState(() {
                          _deviceIndex = value;
                        });
                      },
                    ),
                  ),
                  SizedBox(width: 20),
                  Expanded(
                    flex: 1,
                    child: DropdownButtonFormField(
                      value: _sampleRate,
                      decoration: InputDecoration(labelText: 'Sample Rate'),
                      items: <DropdownMenuItem>[
                        DropdownMenuItem<int>(
                          value: 3,
                          child: SizedBox(
                            width: 200,
                            child: Text('Slowest - 5 FPS'),
                          ),
                        ),
                        DropdownMenuItem<int>(
                          value: 2,
                          child: SizedBox(
                            width: 200,
                            child: Text('Average - 16 FPS'),
                          ),
                        ),
                        DropdownMenuItem<int>(
                          value: 1,
                          child: SizedBox(
                            width: 200,
                            child: Text('Fast - 50 FPS'),
                          ),
                        ),
                        DropdownMenuItem<int>(
                          value: 0,
                          child: SizedBox(
                            width: 200,
                            child: Text('Fastest - no delay'),
                          ),
                        ),
                      ],
                      onChanged: (value) {
                        setState(() {
                          _sampleRate = value;
                        });
                      },
                    ),
                  ),
                ],
              ),
              Row(
                children: <Widget>[
                  Expanded(
                    flex: 1,
                    child: CheckboxListTile(
                      title: const Text('Send Orientation'),
                      value: _sendOrientation,
                      onChanged: (value) {
                        setState(() {
                          _sendOrientation = value;
                        });
                      },
                    ),
                  ),
                  SizedBox(width: 20),
                  Expanded(
                    flex: 1,
                    child: CheckboxListTile(
                      title: const Text('Send Raw Data'),
                      value: _sendRawData,
                      onChanged: (value) {
                        setState(() {
                          _sendRawData = value;
                        });
                      },
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
