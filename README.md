Glasstimote
===========

Demo application for Google glass using Estimote SDK 

ok glass, scan beacons

Starts a service which:
- Starts estimote beacons discovery
- If discovered beacons list size > 0, publishes a live card with number of beacons nearby
- Tap on live card to see beacons details
- Stop option on live card to stop the service
- If service is not stopped it will continue scanning and publish/unpublish live cards as and when beacons are discovered.

Note: It works, mostly, but has issues with the bluetooth stack. Probably a bug in the stack. 
I have reported it. See : https://code.google.com/p/google-glass-api/issues/detail?id=505
