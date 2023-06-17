# kottotto

A discord bot written in Kotlin.

# Features

## OpenJTalk integration

This feature enables kottotto to read aloud messages sent to a text channel.

### Install open-jtalk by homebrew(macOS)

```
brew install open-jtalk
```

then, `open_jtalk` executable should be placed on `/opt/homebrew/opt/open-jtalk/bin/open_jtalk`.
Specify this path to `OPEN_JTALK` environment variable.

### Download MMDAgent

Download MMDAgent
from [here](https://sourceforge.net/projects/mmdagent/files/MMDAgent_Example/MMDAgent_Example-1.8)

### Download Dictionary

Download Dictionary
from [here](https://sourceforge.net/projects/open-jtalk/files/Dictionary/open_jtalk_dic-1.11)

### Configure env variables

```
OPEN_JTALK=/path/to/open_jtalk
OPEN_JTALK_DIC=/path/to/open_jtalk_dic
OPEN_JTALK_HTS_VOICE=/path/to/hts_voice_file
```

## GPT4ALL integration

This feature enables kottotto to chat with you.

### Install GPT4ALL

Follow the official instruction.
official page is [here](https://gpt4all.io)

### Configure env variables

You need to specify the path to the model file.

```
GPT4ALL_MODEL_PATH=/path/to/model
```