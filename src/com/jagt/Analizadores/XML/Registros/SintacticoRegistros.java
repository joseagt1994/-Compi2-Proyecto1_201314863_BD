/* Generated By:JavaCC: Do not edit this line. SintacticoRegistros.java */
package com.jagt.Analizadores.XML.Registros;
import com.jagt.AST.*;
import com.jagt.Logica.*;

import java.util.LinkedList;

public class SintacticoRegistros implements SintacticoRegistrosConstants {

        public int contador = 1;

        public static void main(String args[]) throws ParseException{

                SintacticoRegistros parser = new SintacticoRegistros(System.in);
                parser.inicio();

        }

  final public LinkedList<Registro> inicio() throws ParseException {
 LinkedList<Registro> registros = new LinkedList<Registro>(); Registro r;
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case INICIO_ROW:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      r = fila();
                             registros.add(r);
    }
    jj_consume_token(0);
         {if (true) return registros;}
    throw new Error("Missing return statement in function");
  }

  final public Registro fila() throws ParseException {
 Registro nuevo = new Registro(); Objeto c;
    jj_consume_token(INICIO_ROW);
    label_2:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case INICIO_CAMPO:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_2;
      }
      c = campo();
                         nuevo.agregarColumna(c);
    }
    jj_consume_token(FIN_ROW);
         {if (true) return nuevo;}
    throw new Error("Missing return statement in function");
  }

  final public Objeto campo() throws ParseException {
 Objeto var; Token t;
    t = jj_consume_token(INICIO_CAMPO);
    var = tipo_registro();
    jj_consume_token(FIN_CAMPO);
                                 var.setNombre(t.image.substring(1, t.image.length()-1));
         {if (true) return var;}
    throw new Error("Missing return statement in function");
  }

  final public Objeto tipo_registro() throws ParseException {
 Objeto var; Objeto atr; Objeto nuevo = new Objeto("",SistemaBaseDatos.OBJETO);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case INICIO_CAMPO:
      label_3:
      while (true) {
        atr = atributo();
                                 nuevo.agregarAtributo(atr);
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case INICIO_CAMPO:
          ;
          break;
        default:
          jj_la1[2] = jj_gen;
          break label_3;
        }
      }
         {if (true) return nuevo;}
      break;
    case FALSO:
    case VERDADERO:
    case ENTERO:
    case DOBLE:
    case CADENA:
    case FECHA:
    case HORA:
      atr = expresion();
                                 {if (true) return atr;}
      break;
    default:
      jj_la1[3] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  final public Objeto atributo() throws ParseException {
 Objeto atr;
    jj_consume_token(INICIO_CAMPO);
    atr = expresion();
    jj_consume_token(FIN_CAMPO);
         {if (true) return atr;}
    throw new Error("Missing return statement in function");
  }

  final public Objeto expresion() throws ParseException {
 Objeto atr; Token t;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ENTERO:
      t = jj_consume_token(ENTERO);
                         {if (true) return new Objeto("",t.image,SistemaBaseDatos.ENTERO);}
      break;
    case DOBLE:
      t = jj_consume_token(DOBLE);
                         {if (true) return new Objeto("",t.image,SistemaBaseDatos.DOBLE);}
      break;
    case CADENA:
      t = jj_consume_token(CADENA);
                         {if (true) return new Objeto("",t.image,SistemaBaseDatos.TEXTO);}
      break;
    case FECHA:
      t = jj_consume_token(FECHA);
                         {if (true) return new Objeto("",t.image,SistemaBaseDatos.DATE);}
      break;
    case HORA:
      t = jj_consume_token(HORA);
                         {if (true) return new Objeto("",t.image,SistemaBaseDatos.DATETIME);}
      break;
    case FALSO:
      t = jj_consume_token(FALSO);
                         {if (true) return new Objeto("",t.image,SistemaBaseDatos.BOOL);}
      break;
    case VERDADERO:
      t = jj_consume_token(VERDADERO);
                         {if (true) return new Objeto("",t.image,SistemaBaseDatos.BOOL);}
      break;
    default:
      jj_la1[4] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }

  /** Generated Token Manager. */
  public SintacticoRegistrosTokenManager token_source;
  SimpleCharStream jj_input_stream;
  /** Current token. */
  public Token token;
  /** Next token. */
  public Token jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[5];
  static private int[] jj_la1_0;
  static {
      jj_la1_init_0();
   }
   private static void jj_la1_init_0() {
      jj_la1_0 = new int[] {0x80,0x200,0x200,0x3fa00,0x3f800,};
   }

  /** Constructor with InputStream. */
  public SintacticoRegistros(java.io.InputStream stream) {
     this(stream, null);
  }
  /** Constructor with InputStream and supplied encoding */
  public SintacticoRegistros(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new SintacticoRegistrosTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  /** Reinitialise. */
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  /** Constructor. */
  public SintacticoRegistros(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new SintacticoRegistrosTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  /** Constructor with generated Token Manager. */
  public SintacticoRegistros(SintacticoRegistrosTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  /** Reinitialise. */
  public void ReInit(SintacticoRegistrosTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 5; i++) jj_la1[i] = -1;
  }

  private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }


/** Get the next Token. */
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

/** Get the specific Token. */
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.List<int[]> jj_expentries = new java.util.ArrayList<int[]>();
  private int[] jj_expentry;
  private int jj_kind = -1;

  /** Generate ParseException. */
  public ParseException generateParseException() {
    jj_expentries.clear();
    boolean[] la1tokens = new boolean[21];
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 5; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 21; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.add(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = jj_expentries.get(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  /** Enable tracing. */
  final public void enable_tracing() {
  }

  /** Disable tracing. */
  final public void disable_tracing() {
  }

}