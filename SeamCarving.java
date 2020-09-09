import java.awt.*;
import java.awt.image.BufferedImage;

import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.JFrame;




class SeamCarving {
    static BufferedImage image;
    static int width, height, xArrivee, yArrivee, w, h, nw, nh;
    static int[][] M, eM;

    static final int Infini = Integer.MAX_VALUE; 

    static int[] VerticalSeam, HorizontalSeam;
    static String name;


    // Fonction permettant de récupérer l'image à redimensionner
    public SeamCarving(String fileName, String newWidth, String newHeight) {
        try{
            name = fileName;
            File input = new File(fileName);
            image = ImageIO.read(input);
            width = image.getWidth();
            height = image.getHeight();
            M = eM = new int[height][width];
            xArrivee = yArrivee = 0;

            w = Integer.parseInt(newWidth);
            h = Integer.parseInt(newHeight);
            nw = w*width/100;
            nh = h*height/100;
            VerticalSeam = new int [height];
            HorizontalSeam = new int [width];

        } catch (final Exception e) {
            System.out.println(e);
        }
    }

        // calcul du gradient au carré entre 2 pixels
    static int gradient2(final int ax, final int ay, final int bx, final int by) {
        final Color a = new Color(image.getRGB(ax, ay));
        final Color b = new Color(image.getRGB(bx, by));

        final int ar = a.getRed();
        final int ag = a.getGreen();
        final int ab = a.getBlue();
        final int br = b.getRed();
        final int bg = b.getGreen();
        final int bb = b.getBlue();

        final int e = (int) Math.pow(br - ar, 2) + (int) Math.pow(bg - ag, 2) + (int) Math.pow(bb - ab, 2);
        return e;
    }

    static int energie(final int x, final int y) {
        int gradient2x, gradient2y, e;
        // gradient du bord de l'image sur l'axe y
        if (x == 0) {
            gradient2x = gradient2(x + 1, y, width - 1, y);
            if (y == 0) { // coin supérieur gauche de l'image
                gradient2y = gradient2(x, y + 1, x, height - 1);
            } else if (y == height - 1) { // coin inférieur gauche de l'image
                gradient2y = gradient2(x, 0, x, y - 1);
            } else {
                gradient2y = gradient2(x, y + 1, x, y - 1);
            }
        }

        // gradient du bord de l'image sur l'axe x
        else if (x == width - 1) {
            gradient2x = gradient2(0, y, x - 1, y);

            if (y == 0) { // coin supérieur droit de l'image
                gradient2y = gradient2(x, y + 1, x, height - 1);
            } else if (y == height - 1) { // coin inférieur droit de l'image
                gradient2y = gradient2(x, 0, x, y - 1);
            } else {
                gradient2y = gradient2(x, y + 1, x, y - 1);
            }
        }

        else if (y == 0) {
            gradient2x = gradient2(x + 1, y, x - 1, y);
            gradient2y = gradient2(x, y + 1, x, height - 1);
        }

        else if (y == height - 1) {
            gradient2x = gradient2(x + 1, y, x - 1, y);
            gradient2y = gradient2(x, 0, x, y - 1);
        }

        // gradient au milieu de l'image
        else {
            gradient2x = gradient2(x + 1, y, x - 1, y);
            gradient2y = gradient2(x, y + 1, x, y - 1);
        }

        e = gradient2x + gradient2y;
        return e;
    }

    /*
    * calcul de la matrice d'énergie eM[0:height-1][width-1]
    */
    static void calculerMatriceEnergie(){
        for (int i =0; i < height; i++){
            for (int j = 0; j < width; j++){
                eM[i][j] = energie(j,i);
            }
        }
    }

    static void computeVerticalPath() {
        //base de la récurrence l=0, 1<=c<C
        for(int c=0; c<width; c++){
            M[0][c] = eM[0][c];
        }
        //cas général 1<= l<L, 1<=c<C
        for (int l=1; l<height;l++){
            for (int c =1; c<width; c++){
                if (c == width-1){ 
                    M[l][c] = energie(c,l)+ (int) Math.min(M[l-1][c-1],M[l-1][c]);
                    break;
                }

                else{
                    M[l][c] = (int) Math.min(M[l-1][c+1], (int) Math.min(M[l-1][c-1], M[l-1][c])) + energie(c,l);
                }
            }
            M[l][0] = (int) Math.min (M[l-1][0], M[l-1][1]) + energie(0,l);
        }
    }

    static void computeHorizontalPath() {
        for(int l=0; l<height; l++){
            M[l][0] = energie(0,l);
        }
        for (int c =1; c<width; c++){
            for (int l=1; l<height;l++){
                if (l == height-1){ 
                    M[l][c] = energie(c,l)+ (int) Math.min(M[l-1][c-1],M[l][c-1]);
                    break;
                }
                else{
                    M[l][c] = (int) Math.min(M[l+1][c-1], (int) Math.min(M[l-1][c-1], M[l][c-1])) + energie(c,l);
                }
            }
            M[0][c] = (int) Math.min (M[0][c-1], M[1][c-1]) + energie(0,c);
        }
    }



    static void findVerticalSeam(int l, int c){
        if (l == 0){
            VerticalSeam[0] = c;
            return;
        }
        int Mn = 0, Mno = 0, Mne = 0; // coûts maximum si le dernier déplacement est N, NE ou NO
        if ((l-1) >= 0 && (c + 1 > 0))
            Mno = M[l-1][c+1] +  energie(c,l); // coût maximum si le dernier déplacement est Nord-Ouest
        if ((l-1) >= 0 )
            Mn = M[l-1][c] + energie(c,l);// coût maximum si le dernier déplacement est Nord
        if ((l-1) >= 0 && c-1> 0)
            Mne = M[l-1][c-1] + energie(c,l);// coût maximum si le dernier déplacement est Nord-Est
        if (M[l][c] == Mne)// si le dernier déplacement est Nord-Est
            findVerticalSeam(l-1,c-1);// afficher un chemin de coût maximum jusqu'en case (l-1,c-1)
        else if (M[l][c] == Mn) // si le dernier déplacement est Nord
            findVerticalSeam(l-1,c);// afficher un chemin de coût maximum jusqu'en case (l-1,c)
        else // le dernier déplacement est Nord-Est
            findVerticalSeam(l-1,c+1);// afficher un chemin de coût maximum jusqu'en case (l-1,c+1)

        VerticalSeam[l] = c;
    }


    static void findHorizontalSeam(int l, int c){
        if (c == 0){
            int energieMin, yDepart;
            energieMin = yDepart = 0;
            for (int j = 0; j < width; j++){
                if (M[j][c] < energieMin){
                energieMin = M[j][c];
                yDepart = j;
                }
            }
            HorizontalSeam[0] = yDepart;
            return;
        }
        int Mn = 0, Mno = 0, Mne = 0; // coûts maximum si le dernier déplacement est N, NE ou NO
        if ((l-1) >= 0 && (c - 1 >= 0)) Mno = M[l+1][c-1] +  eM[l][c]; // coût maximum si le dernier déplacement est Nord-Ouest
        if ((c-1) >= 0 ) Mn = M[l][c-1] + eM[l][c];// coût maximum si le dernier déplacement est Nord
        if (l > 0 && c-1>= 0) Mne = M[l-1][c-1] + eM[l][c];// coût maximum si le dernier déplacement est Nord-Est
        
        if (M[l][c] == Mne){ findHorizontalSeam(l-1, c-1);}// si le dernier déplacement est Nord-Est   
        else if (M[l][c] == Mn){
            findHorizontalSeam(l,c-1);// afficher un chemin de coût maximum jusqu'en case (l-1,c+1)
        } // si le dernier déplacement est Nord-Ouest 
        else{
            findHorizontalSeam(l-1,c-1);// afficher un chemin de coût maximum jusqu'en case (l-1,c-1)
        } // le dernier déplacement est Nord-Ouest
    }

    static void findSeam(){
        findVerticalSeam(height-1, xArrivee);
        //findHorizontalSeam(yArrivee, width-1); 
    }
    

    static void ccm(int t){
        int min = 0;
        int coord = 0;

        if (t == height -1){
            for (int c =0; c < width; c++){
                if (M[t][c] < min){
                min = M[t][c];
                coord = c;
                }
            }
            xArrivee = coord;
        }
            
        if (t == width -1){
            for (int l =0; l < height; l++){
                if (M[l][t] < min){
                min = M[l][t];
                coord = l;
                }
            }
            yArrivee = coord;
        }
    }

    static void calculerMinVertical(){
        int min = Infini;
        int coord = 0;
        for (int c =0; c < width; c++){
            if (M[height-1][c] < min){
                min = M[height-1][c];
                coord = c;
            }
        }
        xArrivee = coord;
    }

    
    static void calculerCoordMin(){
        ccm(width-1); //cordonnée de l'arrivée du Seam  horizontal
        ccm(height-1); //coordonnée de l'arrivée du Seam vertical
    }

    /*
    *enlève la seam verticale de l'image en ayant ses coordonnées x
    */
    static void removeVerticalSeam(int[] vertical_seam) {
        BufferedImage updated_img=new BufferedImage(width-1,height,image.getType());
        for(int i=0;i<vertical_seam.length;i++){
            for(int l1=0;l1<vertical_seam[i];l1++)  
                updated_img.setRGB(l1, i, image.getRGB(l1, i));
            for(int l2=vertical_seam[i]+1;l2<width;l2++) 
                updated_img.setRGB(l2-1, i, image.getRGB(l2, i));
        }
        width = width-1;
        image=updated_img;
    }
    
    static void removeAllVerticalSeam(){
        for (int i = 0; i < nw; i++){
            calculerMinVertical();
            computeVerticalPath();
            findSeam();
            removeVerticalSeam(VerticalSeam);
        }
    }

    /*
    * Création d'une nouvelle image correspondant à l'image réduit demandée
    */
    static void newImage(String x, String y){
        try{
            File outputfile = new File(name.substring(0,name.lastIndexOf('.'))+"_"+"resized"+"_"+x+"_"+y+name.substring(name.lastIndexOf('.')));
            ImageIO.write(image, name.substring(name.lastIndexOf('.')+1), outputfile);
            }
            catch(Exception e){
                System.out.println(e);
            }
    }



    public static void main(final String[] args){
        final SeamCarving obj = new SeamCarving ("lake.jpg", "20", "50"); //new SeamCarving(args[0], args[1], args[2]);
        calculerMatriceEnergie();
        computeVerticalPath();
        //computeHorizontalPath();
        calculerCoordMin();
        //findSeam();
        removeAllVerticalSeam();
        newImage("20", "50"); //newImage(args[1], args[2]);
    }
}
