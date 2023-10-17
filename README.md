🌏
Español |
[**English**](./README.en.md)

# Bienvenido a tu nuevo repositorio

> ### NO BORRAR O EDITAR ESTE FICHERO SIN HABERLO LEÍDO ATENTAMENTE

**Contenidos**
- [Introducción](#Introducción)
- [Recomendaciones generales](#Recomendaciones-generales)
- [Recomendaciones específicas según Frameworks y lenguajes de programación](#Recomendaciones-específicas-según-Frameworks-y-lenguajes-de-programación)
  -  [Java, Maven, Graddle, Ant](#Java-Maven-Graddle-Ant)
  -  [C#, .NET, Visual Basic, NuGet, DotNet](#C-NET-Visual-Basic-NuGet-DotNet)
  -  [JavaScript, NodeJS, Angular, VueJS, NPM, Yarn, Bower](#JavaScript-NodeJS-Angular-VueJS-NPM-Yarn-Bower)
  -  [Python](#Python)
  -  [PHP](#PHP)
  -  [iOS, Android, Otros](#iOS-Android-Otros)


## Introducción

A continuación se detallan una serie de configuraciones que deben tenerse en cuenta antes de subir código al repositorio. 

Es importante tener en cuenta estas recomendaciones para que los procesos de verificación de código 
establecidos por Prosegur puedan analizar correctamente el código y aportar el feedback necesario tanto al desarrollador como al equipo del proyecto.

Sólo de este modo se puede detectar cualquier error o vulnerabilidad tanto en fase de desarrollo como en versiones productivas, lo cual es crítico para alertar a los equipos 
para que puedan trabajar en su subsanación y evitar incidentes.

## Recomendaciones generales

- Incluye un fichero **README.md** en el que indiques los aspecto generales de tu proyecto. Algunos puntos a incluir pueden ser los siguientes:

  - **Descripción**: Indicar brevemente para que sirve. Puedes incluir además una línea indicando versión actual, licenciamiento, etc.
  - **Documentación**: Incluye enlaces a la documentación del proyecto.
  - **Requisitos**: Indicar que requisitos y herramientas son necesarias para su compilación y ejecución.
  - **Compilación**: Indicar como se compila el proyecto.
  - **Ejecución**: Indicar como se ejecuta.
  - **Empaquetado**: Indicar como se empaqueta para despliegue.
  - **Despliegue**: Indicar como se despliega.
  - **Configuración**: Indicar los parámetros de configuración más importantes y como utilizarlos.
  - **Versiones**: Indica información sobre las versiones, la funcionalidad incluida, los errores corregidos, compatibilidad, etc.
  - **Soporte**: Incluye información sobre como solicitar soporte o a quien contactar.
  - **Información adicional**: Incluye cualquier otra información relevante que se considere importante como un código de conducta, FAQ, enlaces de interés o relacionados, etc.

- No incluyas claves, password, keys, etc. en el repositorio. Utiliza "secrets" para evitar problemas.
- Utiliza un fichero *.gitignore* en la raiz del repositorio para evitar subir ficheros innecesarios como por ejemplo los que sirven para configurar el IDE privado del desarrollador.
- No crees carpetas innecesarias ni albergues los ficheros de configuración principales en subcarpetas. 
- Utiliza *brach protection rules* para asegurar que no se sube código sin verificar a las ramas principales.
- Utiliza el fichero *CODEOWNERS* bajo la ruta *.github* para indicar que usuarios son propietarios del código y utilizarlo posteriormente en las *branch protection rules*.
- Los ficheros de configuración principales deben estar en la raiz del repositorio para facilitar el trabajo de las herramientas de auto compilación.
- En caso de utilizar librerías o dependencias privadas, necesitar soporte, o si tienes cualquier duda relativa al repositorio, contacta con Soporte al Desarrollo para resolverlas.

## Recomendaciones específicas según Frameworks y lenguajes de programación

Debemos tener en cuenta si el código se ha creado utilizando algún tipo de framework y lenguaje de programación concretos. 

Las herramientas de autocompilación que se utilizan deben ser capaces de reconocer ciertos ficheros que suelen estar presentes cuando se utilizan estos frameworks o lenguajes de programación.

A continuación se indican algunos ejemplos de distintos lenguajes:

### Java, Maven, Graddle, Ant

Los proyectos Java normalmente disponen de un fichero de configuración como ***"pom.xml"*** o ***"build.gradle"*** que contiene las dependencias a utilizar así como los módulos que componen nuestro aplicativo en caso de tratarse de un sistema multicomponente. 

Es importante incluir este fichero en la raiz del repositorio y no situarlo dentro de otra carpeta, ya que esto dificulta la ejecución del auto compilado.

Así mismo, a la hora de construir nuestro proyecto, debemos tener en cuenta los ficheros necesarios a incluir, como el propio fichero de configuración, así como los ficheros JAR, ERA, WAR... necesarios.

### C#, .NET, Visual Basic, NuGet, DotNet

Los proyectos de este tipo suelen disponer de ficheros de configuración en la raiz del proyecto que sirven para identificar el tipo de lenguaje y framework utilizado. 

Por ejemplo, en los proyectos .NET es frecuente disponer de archivos con extensión ***"sln"*** que tienen el contenido de los módulos de la solución, así como la versión de C# utilizada. 

Este tipo de ficheros deben incluirse en el repositorio para que la herramienta de auto compilación pueda realizar el análisis del código.

A la hora de desplegar nuestro aplicativo en el gestor de artefactos, tener en cuenta que deben incluir aquellos elementos necesarios, así como las ***"dll"*** u otros ficheros según el framework utilizado.

### JavaScript, NodeJS, Angular, VueJS, NPM, Yarn, Bower

Este tipo de proyectos, normalmente orientados al desarrollo de interfaces web, suelen incluir un fichero ***"package.json"*** con las dependencias. Es importante incluir tanto este fichero como otros adicionales como pueden ser ***"package-lock.json"***, ***"yarn.lock"***, ***"bower.json"*** o ***"npm-shrinkwrap.json"*** que permiten fijar las versiones de la librerias a utilziar durante la compilación.

### Python

En el caso de aplicaciones Python, normalmente solemos encontrarnos con ficheros como ***"requeriments.txt"*** o ***"Pipfile.lock"*** en la raiz que ayudan a identificar las dependencias y el framework utilizado.

### PHP

En el caso de PHP, para facilitar la compilación se recomienda incluir un fichero ***"composer.lock"*** o ***"composer.json"***.

### iOS, Android, Otros

Para aplicaciones iOS o Android, incluir los ficheros que, según el framework que se utilice, se estimen necesarios para poder compilar el proyecto.

