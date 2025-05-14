# Usa Debian para soporte completo de paquetes
FROM eclipse-temurin:17-jdk

# Definir el directorio de trabajo
WORKDIR /app

# Instalar las dependencias necesarias para Tesseract
RUN apt-get update && apt-get install -y \
    tesseract-ocr \
    tesseract-ocr-spa \
    tesseract-ocr-eng \
    libleptonica-dev \
    && rm -rf /var/lib/apt/lists/*

# Copiar el archivo JAR al contenedor
COPY target/smartbill-backend-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto de la aplicación
EXPOSE 8086

# Configurar la ruta de las librerías nativas para Tesseract
ENV JAVA_OPTS="-Djava.library.path=/usr/lib/x86_64-linux-gnu"
ENV TESSDATA_PREFIX="/usr/share/tesseract-ocr/5/tessdata/"

# Configurar variables de entorno para deshabilitar el debug y establecer el perfil de producción
#ENV LOGGING_LEVEL_ORG_SPRINGFRAMEWORK=info
#ENV LOGGING_LEVEL_ROOT=info

# Ejecutar la aplicación utilizando el formato de shell para que se expanda la variable
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
