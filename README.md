# MapboxMinZoom
This repository sample app demonstrates an issue with view changing the minZoom of layers

# Usage
- add the `MAPBOX_DOWNLOADS_TOKEN` as property to a file called `local.properties` in your root folder
- Run the app and observe the `water` layer to be missing because it is set to `minZoom=22` initially. Press the `change minZoom...` button and observe the water layer not being displayed. An animation will start which animates out a little, which makes the water layer appear.

