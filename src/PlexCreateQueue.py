import argparse
import pychromecast
import time
from plexapi.myplex import PlexServer
"""
Controller to interface with the Plex-app.
"""
import json
import threading

from copy import deepcopy
from urllib.parse import urlparse

from pychromecast.controllers import BaseController


parser = argparse.ArgumentParser(description="Example on how to use the Plex Controller.")
parser.add_argument('--url', help='Plex Server URL', required=True)
parser.add_argument('--access_token', help='Plex Server Access Token', required=True)
parser.add_argument('--mediaID', help='Plex Server Media ID', required=True)
args = parser.parse_args()

baseurl = args.url
token = args.access_token
id=args.mediaID

plex = PlexServer(baseurl, token)

cars = plex.library.search(id=id)
a = plex.createPlayQueue(cars[0])
print(a.playQueueID)
