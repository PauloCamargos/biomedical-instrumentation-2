#include "SoftwareSerial.h"
#include "Statistics.h"
//https://github.com/provideyourown/statistics
#include "pitches.h"
#include "TimerOne.h"

// PARAMETROS CONFIGURAVEIS
#define FA 10               // frequencia de amostragem [hz]
#define QNT_AMOSTRAS 10        // qnt amostras no vetor

// CONSTANTES
#define DELIMITADOR '$'
#define START 'S'
#define END 'E'

// PARAMETROS DO PROGRAMA
#define TX 11             // pino tx (blth)
#define RX 12             // pino rx (blth)
#define ANALOG_PIN A7     // pino leitura do tecido
#define SOUND_PIN 13      // ouput buzzer
#define NOTE_FRACTION 20  // fracao de segundo do som
#define NOTE NOTE_A7      // nota musical do buzzer
#define BAUD_RATE 115200  // baud rate

// instanciando pino tx e rx do bluetooth
SoftwareSerial blth(RX, TX);

// instanciando Statistics com 10 valores
Statistics breath_signal(QNT_AMOSTRAS);
Statistics stdev_calibration(10.0 * FA);

unsigned long time_now = 0;
unsigned long time_to_alarm = 0;

int val;
float meanVal;
float maxVal;
float minVal;
float stdDev;
float std_alarm = 15;        // desvio padrao limite para alarme
int tempo_alarme = 5;          // tempo para disparo de alarme [s]
int limiar_std = 0;

float TA = (1.0 / FA) * 1000000.0; // tempo de amostragem em us

char lixo;
String data = "";
int counter = 0;

void setup() {
  //inciando comunicacao no serial monitor e bluetooth
  Serial.begin(BAUD_RATE);
  blth.begin(9600);
  // iniciando estatistica (stat)
  breath_signal.reset();
  stdev_calibration.reset();
}

void loop() {
  if (blth.available()) {
    char incomingChar = (char) blth.read();
    if (incomingChar == DELIMITADOR) {
      char commandChar = (char) blth.read();
      Serial.println(commandChar);
    }
//      switch (commandChar) {
//        case START:
//          String tempo_limite;
//          String alarme_limite;
//          lixo = blth.read();
//          do {
//            incomingChar = blth.read();
//            tempo_limite += incomingChar;
//          } while (incomingChar != DELIMITADOR);
//
//          do {
//            incomingChar = blth.read();
//            alarme_limite += incomingChar;
//          } while (incomingChar != END);
//          tempo_alarme = tempo_limite.toInt();
//          limiar_std = alarme_limite.toInt();
//          Serial.println(tempo_limite);
//          Serial.println(alarme_limite);
//          break;
//        case END:
//          break;
//      }

    }

    //    if(charDelimitador == DELIMITADOR){
    //      char charComando = (char) blth.read();
    //      if(charComando == 'S'){
    //        Serial.println(charComando);
    //      }
  }

  //    while (blth.available()) {
  //      char inChar = (char) blth.read();
  //      data += inChar;
  //    }
  //    Serial.print(data);
  //
  //    while (blth.available() > 0) {
  //      lixo = blth.read();
  //    }
  //    blth.flush();
  //    if (data == START) {
  //      Timer1.initialize(TA);
  //      Timer1.attachInterrupt(sendSignal);
  //    } else if (data == END) {
  //      Timer1.detachInterrupt();
  //    }
  //    data = "";



void sendSignal() {
  //  time_now = millis();
  maxVal = breath_signal.maxVal();
  minVal = breath_signal.minVal();
  //  if (millis() < time_now + TA) {

  // valor lido do tecido, entre 0-255
  val = analogRead(ANALOG_PIN);
  //  byte mappedVal = map(val,40,70,0,255);

  // incluindo o valor no array de stat
  breath_signal.addData(val);

  // media e desvio padrao
  meanVal = breath_signal.mean();
  stdDev = breath_signal.stdDeviation();
  // Calibrando nos 10s iniciais
  if (counter <= (10 * FA)) {
    counter++;
    stdev_calibration.addData(stdDev);
  } else if (counter == (10 * FA)) {
    std_alarm = limiar_std * stdev_calibration.mean();
  }

  byte mappedMean = map(meanVal, minVal, maxVal, 0, 255);

  //   soa alarme de desvio padrao < 40
  if (stdDev <= std_alarm) {
    if (millis() > (time_to_alarm + tempo_alarme * 1000)) {
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

  blth.print(mappedMean);
  blth.print("\n");
}

void triggerAlarm() {
  int noteDuration = 1000 / NOTE_FRACTION;
  tone(13, NOTE , noteDuration);
}
