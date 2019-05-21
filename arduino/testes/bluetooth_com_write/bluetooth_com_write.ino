#include "SoftwareSerial.h"
#include "Statistics.h"
//https://github.com/provideyourown/statistics
#include "pitches.h"

// PARAMETROS CONFIGURAVEIS
#define FA 10               // frequencia de amostragem [hz]
#define QNT_AMOSTRAS 100        // qnt amostras no vetor

// PARAMETROS DO PROGRAMA
#define TX 11             // pino tx (ard)
#define RX 12             // pino rx (ard)
#define ANALOG_PIN A7     // pino leitura do tecido
#define SOUND_PIN 13      // ouput buzzer
#define NOTE_FRACTION 20  // fracao de segundo do som
#define NOTE NOTE_A7      // nota musical do buzzer
#define BAUD_RATE 115200  // baud rate

// instanciando pino tx e rx do bluetooth
SoftwareSerial blth(RX, TX);

// instanciando Statistics com 10 valores
Statistics stat(QNT_AMOSTRAS);
int count = 0;
float TA = (1.0 / FA) * 1000.0; // tempo de amostragem em ms
unsigned long time_now = 0;
unsigned long time_to_alarm = 0;

int val;
float meanVal;
float maxVal;
float minVal;
float stdDev;
float std_alarm = 15;        // desvio padrao limite para alarme
float carencia = 5;          // tempo para disparo de alarme [s]

void setup() {
  //inciando comunicacao no serial monitor e bluetooth
  Serial.begin(BAUD_RATE);
  //  blth.begin(9600);
  // iniciando estatistica (stat)
  stat.reset();
}

void loop() {
  time_now = millis();
  if (millis() < time_now + TA) {

    // valor lido do tecido, entre 0-255
    val = analogRead(ANALOG_PIN);
    //  byte mappedVal = map(val,40,70,0,255);

    // incluindo o valor no array de stat
    stat.addData(val);

    // media e desvio padrao
    meanVal = stat.mean();
    stdDev = stat.stdDeviation();

    byte mappedMean = map(meanVal, 0, 1023, 0, 255);

    // soa alarme de desvio padrao < 40
    if (stdDev <= std_alarm) {
      if (millis() > (time_to_alarm + carencia * 1000)) {
        triggerAlarm();
      }
    } else {
      time_to_alarm = millis();
    }
    
    Serial.print(0);  // To freeze the lower limit
    Serial.print(" ");
    Serial.print(255);  // To freeze the upper limit
    Serial.print(" ");
    Serial.print(mappedMean);
    Serial.print(" ");
    Serial.println(stdDev);

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
