
//123456 123456
public class Functions{
    private double minAcc=1,maxAcc=1,minGyro=1,maxGyro=1;
    private int[] dataset;
    private double[] average;
    private int[]data;
    private int window=0;

  public Functions(int window){
    this.window= window;
    average = new double[(window*6)+1];
    data = new int[(window*6)+1];
  }
    public void movingAverage(){

        for(int i=0; i<window;i++){

            if(i==0){
                average[i] = dataset[0];
                average[i+1] = dataset[1];
                average[i+2] = dataset[2];
                average[i+3] = dataset[3];
                average[i+4] = dataset[4];
                average[i+5] = dataset[5];
            }
            else if(i==1){
                average[(i*6)]   = ((dataset[(i*6)]+dataset[0])/2);
                average[(i*6)+1] = ((dataset[(i*6)+1]+dataset[1])/2);
                average[(i*6)+2] = ((dataset[(i*6)+2]+dataset[2])/2);
                average[(i*6)+3] = ((dataset[(i*6)+3]+dataset[3])/2);
                average[(i*6)+4] = ((dataset[(i*6)+4]+dataset[4])/2);
                average[(i*6)+5] = ((dataset[(i*6)+5]+dataset[5])/2);
            }
            else if(i==2){
                average[(i*6)]   = ((dataset[(i*6)]+dataset[6]+dataset[0])/3);
                average[(i*6)+1] = ((dataset[(i*6)+1]+dataset[7]+dataset[1])/3);
                average[(i*6)+2] = ((dataset[(i*6)+2]+dataset[8]+dataset[2])/3);
                average[(i*6)+3] = ((dataset[(i*6)+3]+dataset[9]+dataset[3])/3);
                average[(i*6)+4] = ((dataset[(i*6)+4]+dataset[10]+dataset[4])/3);
                average[(i*6)+5] = ((dataset[(i*6)+5]+dataset[11]+dataset[5])/3);
            }
            else if(i==3){
                average[(i*6)]   = ((dataset[(i*6)]+dataset[(12)]+dataset[6]+dataset[0])/4);
                average[(i*6)+1] = ((dataset[(i*6)+1]+dataset[13]+dataset[7]+dataset[1])/4);
                average[(i*6)+2] = ((dataset[(i*6)+2]+dataset[14]+dataset[8]+dataset[2])/4);
                average[(i*6)+3] = ((dataset[(i*6)+3]+dataset[15]+dataset[9]+dataset[3])/4);
                average[(i*6)+4] = ((dataset[(i*6)+4]+dataset[16]+dataset[10]+dataset[4])/4);
                average[(i*6)+5] = ((dataset[(i*6)+5]+dataset[17]+dataset[11]+dataset[5])/4);
            }
    /*else if(i==4){
        average[(i*6)]   = ((dataset[(i*6)]+dataset[18]+dataset[12]+dataset[6]+dataset[0])/5);
        average[(i*6)+1] = ((dataset[(i*6)]+dataset[19]+dataset[13]+dataset[7]+dataset[1])/5);
        average[(i*6)+2] = ((dataset[(i*6)]+dataset[20]+dataset[14]+dataset[8]+dataset[2])/5);
        average[(i*6)+3] = ((dataset[(i*6)]+dataset[21]+dataset[15]+dataset[9]+dataset[3])/5);
        average[(i*6)+4] = ((dataset[(i*6)]+dataset[22]+dataset[16]+dataset[10]+dataset[4])/5);
        average[(i*6)+5] = ((dataset[(i*6)]+dataset[23]+dataset[17]+dataset[11]+dataset[5])/5);
    }*/
            else{
                average[(i*6)]   = ((dataset[(i*6)]+dataset[((i-1)*6)]+dataset[((i-2)*6)]+dataset[((i-3)*6)]+dataset[((i-4)*6)])/5);
                average[(i*6)+1] = ((dataset[(i*6)+1]+dataset[((i-1)*6)+1]+dataset[((i-2)*6)+1]+dataset[((i-3)*6)+1]+dataset[((i-4)*6)+1])/5);
                average[(i*6)+2] = ((dataset[(i*6)+2]+dataset[((i-1)*6)+2]+dataset[((i-2)*6)+2]+dataset[((i-3)*6)+2]+dataset[((i-4)*6)+2])/5);
                average[(i*6)+3] = ((dataset[(i*6)+3]+dataset[((i-1)*6)+3]+dataset[((i-2)*6)+3]+dataset[((i-3)*6)+3]+dataset[((i-4)*6)+3])/5);
                average[(i*6)+4] = ((dataset[(i*6)+4]+dataset[((i-1)*6)+4]+dataset[((i-2)*6)+4]+dataset[((i-3)*6)+4]+dataset[((i-4)*6)+4])/5);
                average[(i*6)+5] = ((dataset[(i*6)+5]+dataset[((i-1)*6)+5]+dataset[((i-2)*6)+5]+dataset[((i-4)*6)+5]+dataset[((i-4)*6)+5])/5);
            }
        }
    }

    public void maxMin(){

        for(int i=0;i<window;i++){
            for (int j=0;j<6;j++) {
                if (j<3) {
                    if (minAcc>dataset[i*j]) {
                        minAcc = dataset[i*j];
                    }
                    if (maxAcc<dataset[i*j]) {
                        maxAcc = dataset[i*j];
                    }
                }
                else{
                    if (minGyro>dataset[i*j]) {
                        minGyro = dataset[i*j];
                    }
                    if (maxGyro<dataset[i*j]) {
                        maxGyro = dataset[i*j];
                    }
                }
            }
        }
        System.out.println("MinAcc: "+minAcc);
        System.out.println("MaxAcc: "+maxAcc);
        System.out.println("MinGyro: "+minGyro);
        System.out.println("MaxGyro: "+maxGyro);
    }

    public void normalize(){
        System.out.println("Normalized Data: ");
        for(int i=0;i<window;i++){
            for (int j=0;j<6;j++) {
                if (j<3) {
                    data[(i*6)+j] = (int)((((average[(i*6)+j]-minAcc)/(maxAcc-minAcc))*(100-0))+0);
                }
                else{
                    data[(i*6)+j] = (int)((((average[(i*6)+j]-minGyro)/(maxGyro-minGyro))*(100-0))+0);
                }
                System.out.println(dataset[i+j]);

            }

        }
    }
    
    public int[] getData(int[] arr){
        this.dataset = arr;
        movingAverage();
        maxMin();
        normalize();
        return data;
    }
}