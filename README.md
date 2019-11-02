# Smart Prediction

## Introduction

Smart Prediction(Suggestion) is an Android App which predicts shortcuts for App, Call & SMS based on user's context. It will display a bubble (similar to messenger) and can launch the shortcut. It collects the data from operating system and prepare predictions using clustering algorithms. This is still in research stage and the results might not be highly accurate. Since the app works on in-device, internet connection is not required to work. However, in order to improve my research, collected data is uploaded anonymously. The contact numbers are encrypted using an device specific encryption key and uploaded data does not reveal any contact number.

## Getting Started

1. Launch android studio
2. Click on File, then New and then Import Project Or from the welcome screen of android studio click on Import project. (If you wish to contribute, you can directly link to GitHub)
3. Browse to the directory where you cloned (extracted the zip) research-prediction App and press OK
4. Let Android studio import the project, sync, run and build the Gradle.
5. If Gradle finishes to build without error run the project by clicking on the play button on the tool bar.
6. Wait for a few seconds and the app should start on your device(android phone or emulator)


## Contributing
You can either contribute as a developer or a tester

#### As a developer
You can go through the issues and comment on the issue which you need to work on. Do not try to work on an issue which somebody is working on. Then fix/complete the issue and send a Pull Request (PR). After the review it will be merged to the repository. 
When comitting changes please use following format.

```
[<issue type>] <description>
<mandatory line break>
<Long description if need further explaination than the description>
```

Example:
```
[fix] Fix the issue Exception issue when trying to retrieve prediction

If the clustering is not clompleted, invoking the retrive predictions will throw an Exception.
```

Here are the issue types which can be used. 

* feat: a new feature
* fix: a bug fix
* docs: changes to documentation
* style: formatting, missing semi colons, etc; no code change
* refactor: refactoring production code
* test: adding tests, refactoring test; no production code change
* chore: updating build tasks, package manager configs, etc; no production code change

Description of the commit message should sound commanding like Fix, Add. Make sure the description will not use more than 70 characters. If you need to add further explaination, mention them in the long description

#### As a tester

Build the application and run on your device and let us know if you encounter an error/improvement. Report them on issues and if the issue is valid, developers can start to work on them.

## License

```
MIT License

Copyright (c) 2017 Chamika Weerasinghe

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```