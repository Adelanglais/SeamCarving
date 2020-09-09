# SeamCarving

Dans le cadre du cours d'Algorithmique proposé par M. Natowicz à l'ESIEE Paris,ce programme met en oeuvre la méthode  
du SeamCarving utilisée pour le redimensionnent d'image. Afin d'éviter de modifier le fond, les formes et plus géné-  
ralement,afin de limiter la perte d'information (image déformée, proportions non respéctées, effet zigzag...etc), les
pixels de plus faible importance seront les premiers à être retirés. Pour les déterminer, nous utiliserons une fonc-
tion énergie et les seam verticaux et horizontaux de l’image en suivant l’approche du “one directional case”.

## Usage
```java
javac SeamCarving.java
java SeamCarving # retourne une nouvelle image redimensionnée
```

