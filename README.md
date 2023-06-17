# macOS

## install open-jtalk by homebrew

```
brew install open-jtalk
```

then, `open_jtalk` executable should be placed on `/opt/homebrew/opt/open-jtalk/bin/open_jtalk`.
Specify this path to `OPEN_JTALK` environment variable.

## Download MMDAgent

Download MMDAgent
from [here](https://sourceforge.net/projects/mmdagent/files/MMDAgent_Example/MMDAgent_Example-1.8/MMDAgent_Example-1.8.zip/download).

## Download Dictionary

Download Dictionary
from [here](https://sourceforge.net/projects/open-jtalk/files/Dictionary/open_jtalk_dic-1.11/open_jtalk_dic_utf_8-1.11.tar.gz/download).

## Configure env variables

```
OPEN_JTALK=/path/to/open_jtalk
OPEN_JTALK_DIC=/path/to/open_jtalk_dic
OPEN_JTALK_HTS_VOICE=/path/to/hts_voice_file
```