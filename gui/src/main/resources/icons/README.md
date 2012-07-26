OpenPnP Icon Specifications
===========================


Common Themes
-------------
The current icon set has some common elements that are used across multiple
icons. It is not necessary to use these exact same elements, but some thought
should be given to maintaining a set of common elements across sets of features
that are shared.

### Capturing
Several icons express the concept of capturing data from one place and storing
it in another. For instance, there is an icon that captures the current
coordinates of the camera and stores them in a location field.

Capturing is identified by a square, blue outline as the primary design element.

### Positioning
Icons that are used to indicate that the machine will perform a positioning
movement are identified by a round, red outline. Red is used to provide a
warning to the user that the machine will make movement in response to
performing the action.

### Tool, Actuator and Camera
Several of the above icons are intended to perform their action with regards
to either the tool, actuator or camera. Where possible, these icons all use
a common element across their different functions.

The tool design element is a broken crosshair; four lines extending from the
edges of the outline and not quite meeting in the center.

The actuator design element is similar to that of the tool, except the lines
leave a larger open area in the center and it is filled with a large dot
indicating the actuator pin.

The camera design element is intended to invoke a camera viewfinder. It is an
interrupted rectangle, centered within the outline.


Icon List
---------
This is a list of the filenames of the icons currently being used in the system
along with descriptions of each. This can be used as a reference for designing
a new icon set.

* capture-camera.png

Captures the coordinates of the camera and inserts them into a location field.
		
* capture-pin.png

Captures the coordinates of the pin/actuator and inserts them into a location
field.

* capture-tool.png

Captures the coordinates of the tool and inserts them into a location field.

* capture-tool-z.png

Captures only the Z coordinate of the tool and inserts it into a location
field.

* center-camera.png

Positions the camera to be in focus over a highlighted location.

* center-tool.png

Positions the tool so that it is touching the highlighted location.

* center-pin.png

Positions the pin so that it is touching the highlighted location.

