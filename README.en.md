🌏
[**Español**](./README.md) |
English

# Welcome to your new repository

> ### DO NOT DELETE OR EDIT THIS FILE WITHOUT READING IT CAREFULLY

**Contents**
- [Introduction](#Introduction)
- [General recomendations](#General-recommendations)
- [Specific recommendations according to Frameworks and programming languages](#Specific-recommendations-according-to-Frameworks-and-programming-languages)
  -  [Java, Maven, Graddle, Ant](#Java-Maven-Graddle-Ant)
  -  [C#, .NET, Visual Basic, NuGet, DotNet](#C-NET-Visual-Basic-NuGet-DotNet)
  -  [JavaScript, NodeJS, Angular, VueJS, NPM, Yarn, Bower](#JavaScript-NodeJS-Angular-VueJS-NPM-Yarn-Bower)
  -  [Python](#Python)
  -  [PHP](#PHP)
  -  [iOS, Android, Others](#iOS-Android-Others) 


## Introduction

Below are a series of settings that must be taken into account before uploading code to the repository.

It's important to take these recommendations into account so that code verification processes
established by Prosegur can correctly analyze the code and provide the necessary feedback to developers and project teams.

Only in this way can any error or vulnerability be detected both in the development phase and in production versions, which is critical to alert teams
so that they can work on their correction and avoid incidents.

## General recommendations

- Includes a **README.md** file in which you indicate the general aspects of your project. Some points to include may be the following:

   - **Description**: Briefly indicate what it's for. You can also include a line indicating current version, licensing, etc.
   - **Documentation**: Includes links to the project documentation.
   - **Requirements**: Indicate what requirements and tools are necessary for its compilation and execution.
   - **Compilation**: Indicate how the project is compiled.
   - **Execution**: Indicate how it's executed.
   - **Packaged**: Indicate how it's packaged for deployment.
   - **Deployment**: Indicate how it's deployed.
   - **Configuration**: Indicate the most important configuration parameters and how to use them.
   - **Versions**: Indicates information about the versions, the functionality included, the bugs fixed, compatibility, etc.
   - **Support**: Includes information on how to request support or who to contact.
   - **Additional information**: Includes any other relevant information that is considered important such as a code of conduct, FAQ, interest or related links, etc.

- Do not include keys, password, etc. in the repository. Use "secrets" to avoid problems.
- Use a *.gitignore* file in the root of the repository to avoid uploading unnecessary files such as those used to configure the developer's private IDE.
- Do not create unnecessary folders or store the main configuration files in subfolders.
- Use *brach protection rules* to ensure that unchecked code is not pushed to main branches.
- Use the *CODEOWNERS* file under the *.github* path to indicate which users own the code and use it later in the *branch protection rules*.
- The main configuration files must be in the root of the repository to facilitate the work of the auto-compilation tools.
- If you use private libraries or dependencies, need support, or if you have any questions regarding the repository, contact Development Support from Architecture area to resolve them.

## Specific recommendations according to Frameworks and programming languages

We must take into account if the code has been created using some type of framework and specific programming language.

The autocompilation tools that are used must be able to recognize certain files that are usually present when these frameworks or programming languages are used.

Here are some examples from different languages:

### Java, Maven, Graddle, Ant

Java projects normally have a configuration file such as ***"pom.xml"*** or ***"build.gradle"*** that contains the dependencies to use as well as the modules that make up our application in case being a multi-component system.

It's important to include this files in the root of the repository and not place it inside another folder, since this makes it difficult to run the auto compile.

Likewise, when building our project, we must take into account the necessary files to include, such as the configuration file itself, as well as the necessary JAR, ERA, WAR... files.

### C#, .NET, Visual Basic, NuGet, DotNet

Projects of this type usually have configuration files in the root of the project that serve to identify the type of language and framework used.

For example, in .NET projects it is common to have files with the extension ***"sln"*** that contain the content of the solution modules, as well as the version of C# used.

These types of files must be included in the repository so that the autobuild tool can perform code analysis.

When deploying our application in the artifact manager, keep in mind that they must include those necessary elements, as well as the ***"dll"*** or other files depending on the framework used.

### JavaScript, NodeJS, Angular, VueJS, NPM, Yarn, Bower

This type of project, normally oriented to the development of web interfaces, usually includes a ***"package.json"*** file with the dependencies. It is important to include both this file and additional ones such as ***"package-lock.json"***, ***"yarn.lock"***, ***"bower.json"*** or ***"npm-shrinkwrap.json"*** that allow you to set the library versions to be used during compilation.

### Python

In the case of Python applications, we usually find files like ***"requeriments.txt"*** or ***"Pipfile.lock"*** in the root that help identify dependencies and the framework used.

### PHP

In the case of PHP, to facilitate compilation it is recommended to include a ***"composer.lock"*** or ***"composer.json"*** file.

### iOS, Android, Others

For iOS or Android applications, include the files that, depending on the framework used, are deemed necessary to compile the project.
