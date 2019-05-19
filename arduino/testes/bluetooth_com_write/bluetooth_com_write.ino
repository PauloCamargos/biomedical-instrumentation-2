#include "SoftwareSerial.h"
#include "Statistics.h"
//https://github.com/provideyourown/statistics
#include "pitches.h"

#define fa 100 // frequencia de amostragem em hz

#define TX 11 // pino tx
#define RX 12 // pino rx
#define ANALOG_PIN A7 // leitua do tecido
#define SOUND_PIN 13 // ouput buzzer
#define NOTE_FRACTION 20 // fracao de segundo do som
#define NOTE NOTE_A7 // nota musical do buzzer

// instanciando pino tx e rx do bluetooth
SoftwareSerial blth(RX, TX);

// instanciando Statistics com 10 valores
Statistics stat(50);
int count = 0;
float ta = (1.0 / fa) * 1000.0; // tempo de amostragem em ms
unsigned long time_now = 0;

void setup() {
  //inciando comunicacao no serial monitor e bluetooth
  Serial.begin(115200);
//  blth.begin(9600);

  // iniciando estatistica (stat)
  stat.reset();
}

void loop() {
  float meanVal;
  float stdDev;
  // Quando o tecido eh distendido, a resistência cai
  // A res cai a qual taxa?
  // Exite um tempo de recuperação do tecido?
  // Qual a durabilidade do tecido?

  time_now = millis();
  if (millis() < time_now + ta) {

    // valor lido do tecido, entre 0-255
    int val = analogRead(ANALOG_PIN);
    //  byte mappedVal = map(val,40,70,0,255);

    // incluindo o valor no array de stat
    stat.addData(val);

    // media e desvio padrao
    meanVal = stat.mean();
    stdDev = stat.stdDeviation();

    int mappedMean = map(meanVal, 0, 1023, 0, 255);

    // soa alarme de desvio padrao < 40
    if (stdDev <= 40) {
      triggerAlarm();
    }
      Serial.println(meanVal);
//      Serial.print(","); 
//      Serial.println(stdDev);

//    blth.print(meanVal);
//    blth.print("\n");
  }

}

void triggerAlarm() {
  int noteDuration = 1000 / NOTE_FRACTION;
  tone(13, NOTE , noteDuration);
}
