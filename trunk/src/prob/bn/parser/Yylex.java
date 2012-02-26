package prob.bn.parser;


class Yylex {
	private final int YY_BUFFER_SIZE = 512;
	private final int YY_F = -1;
	private final int YY_NO_STATE = -1;
	private final int YY_NOT_ACCEPT = 0;
	private final int YY_START = 1;
	private final int YY_END = 2;
	private final int YY_NO_ANCHOR = 4;
	private final int YY_BOL = 65536;
	private final int YY_EOF = 65537;

public int yyline() { return yyline; } 
	private java.io.BufferedReader yy_reader;
	private int yy_buffer_index;
	private int yy_buffer_read;
	private int yy_buffer_start;
	private int yy_buffer_end;
	private char yy_buffer[];
	private int yychar;
	private int yyline;
	private boolean yy_at_bol;
	private int yy_lexical_state;

	Yylex (java.io.Reader reader) {
		this ();
		if (null == reader) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(reader);
	}

	Yylex (java.io.InputStream instream) {
		this ();
		if (null == instream) {
			throw (new Error("Error: Bad input stream initializer."));
		}
		yy_reader = new java.io.BufferedReader(new java.io.InputStreamReader(instream));
	}

	private Yylex () {
		yy_buffer = new char[YY_BUFFER_SIZE];
		yy_buffer_read = 0;
		yy_buffer_index = 0;
		yy_buffer_start = 0;
		yy_buffer_end = 0;
		yychar = 0;
		yyline = 0;
		yy_at_bol = true;
		yy_lexical_state = YYINITIAL;
	}

	private boolean yy_eof_done = false;
	private final int YYINITIAL = 0;
	private final int yy_state_dtrans[] = {
		0
	};
	private void yybegin (int state) {
		yy_lexical_state = state;
	}
	private int yy_advance ()
		throws java.io.IOException {
		int next_read;
		int i;
		int j;

		if (yy_buffer_index < yy_buffer_read) {
			return yy_buffer[yy_buffer_index++];
		}

		if (0 != yy_buffer_start) {
			i = yy_buffer_start;
			j = 0;
			while (i < yy_buffer_read) {
				yy_buffer[j] = yy_buffer[i];
				++i;
				++j;
			}
			yy_buffer_end = yy_buffer_end - yy_buffer_start;
			yy_buffer_start = 0;
			yy_buffer_read = j;
			yy_buffer_index = j;
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}

		while (yy_buffer_index >= yy_buffer_read) {
			if (yy_buffer_index >= yy_buffer.length) {
				yy_buffer = yy_double(yy_buffer);
			}
			next_read = yy_reader.read(yy_buffer,
					yy_buffer_read,
					yy_buffer.length - yy_buffer_read);
			if (-1 == next_read) {
				return YY_EOF;
			}
			yy_buffer_read = yy_buffer_read + next_read;
		}
		return yy_buffer[yy_buffer_index++];
	}
	private void yy_move_end () {
		if (yy_buffer_end > yy_buffer_start &&
		    '\n' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
		if (yy_buffer_end > yy_buffer_start &&
		    '\r' == yy_buffer[yy_buffer_end-1])
			yy_buffer_end--;
	}
	private boolean yy_last_was_cr=false;
	private void yy_mark_start () {
		int i;
		for (i = yy_buffer_start; i < yy_buffer_index; ++i) {
			if ('\n' == yy_buffer[i] && !yy_last_was_cr) {
				++yyline;
			}
			if ('\r' == yy_buffer[i]) {
				++yyline;
				yy_last_was_cr=true;
			} else yy_last_was_cr=false;
		}
		yychar = yychar
			+ yy_buffer_index - yy_buffer_start;
		yy_buffer_start = yy_buffer_index;
	}
	private void yy_mark_end () {
		yy_buffer_end = yy_buffer_index;
	}
	private void yy_to_mark () {
		yy_buffer_index = yy_buffer_end;
		yy_at_bol = (yy_buffer_end > yy_buffer_start) &&
		            ('\r' == yy_buffer[yy_buffer_end-1] ||
		             '\n' == yy_buffer[yy_buffer_end-1] ||
		             2028/*LS*/ == yy_buffer[yy_buffer_end-1] ||
		             2029/*PS*/ == yy_buffer[yy_buffer_end-1]);
	}
	private java.lang.String yytext () {
		return (new java.lang.String(yy_buffer,
			yy_buffer_start,
			yy_buffer_end - yy_buffer_start));
	}
	private int yylength () {
		return yy_buffer_end - yy_buffer_start;
	}
	private char[] yy_double (char buf[]) {
		int i;
		char newbuf[];
		newbuf = new char[2*buf.length];
		for (i = 0; i < buf.length; ++i) {
			newbuf[i] = buf[i];
		}
		return newbuf;
	}
	private final int YY_E_INTERNAL = 0;
	private final int YY_E_MATCH = 1;
	private java.lang.String yy_error_string[] = {
		"Error: Internal error.\n",
		"Error: Unmatched input.\n"
	};
	private void yy_error (int code,boolean fatal) {
		java.lang.System.out.print(yy_error_string[code]);
		java.lang.System.out.flush();
		if (fatal) {
			throw new Error("Fatal Error.\n");
		}
	}
	private int[][] unpackFromString(int size1, int size2, String st) {
		int colonIndex = -1;
		String lengthString;
		int sequenceLength = 0;
		int sequenceInteger = 0;

		int commaIndex;
		String workString;

		int res[][] = new int[size1][size2];
		for (int i= 0; i < size1; i++) {
			for (int j= 0; j < size2; j++) {
				if (sequenceLength != 0) {
					res[i][j] = sequenceInteger;
					sequenceLength--;
					continue;
				}
				commaIndex = st.indexOf(',');
				workString = (commaIndex==-1) ? st :
					st.substring(0, commaIndex);
				st = st.substring(commaIndex+1);
				colonIndex = workString.indexOf(':');
				if (colonIndex == -1) {
					res[i][j]=Integer.parseInt(workString);
					continue;
				}
				lengthString =
					workString.substring(colonIndex+1);
				sequenceLength=Integer.parseInt(lengthString);
				workString=workString.substring(0,colonIndex);
				sequenceInteger=Integer.parseInt(workString);
				res[i][j] = sequenceInteger;
				sequenceLength--;
			}
		}
		return res;
	}
	private int yy_acpt[] = {
		/* 0 */ YY_NO_ANCHOR,
		/* 1 */ YY_NO_ANCHOR,
		/* 2 */ YY_NO_ANCHOR,
		/* 3 */ YY_NO_ANCHOR,
		/* 4 */ YY_NO_ANCHOR,
		/* 5 */ YY_NO_ANCHOR,
		/* 6 */ YY_NO_ANCHOR,
		/* 7 */ YY_NO_ANCHOR,
		/* 8 */ YY_NO_ANCHOR,
		/* 9 */ YY_NO_ANCHOR,
		/* 10 */ YY_NO_ANCHOR,
		/* 11 */ YY_NO_ANCHOR,
		/* 12 */ YY_NO_ANCHOR,
		/* 13 */ YY_NO_ANCHOR,
		/* 14 */ YY_NO_ANCHOR,
		/* 15 */ YY_NO_ANCHOR,
		/* 16 */ YY_NO_ANCHOR,
		/* 17 */ YY_NO_ANCHOR,
		/* 18 */ YY_NO_ANCHOR,
		/* 19 */ YY_NO_ANCHOR,
		/* 20 */ YY_NO_ANCHOR,
		/* 21 */ YY_NO_ANCHOR,
		/* 22 */ YY_NO_ANCHOR,
		/* 23 */ YY_NO_ANCHOR,
		/* 24 */ YY_NO_ANCHOR,
		/* 25 */ YY_NO_ANCHOR,
		/* 26 */ YY_NOT_ACCEPT,
		/* 27 */ YY_NO_ANCHOR,
		/* 28 */ YY_NO_ANCHOR,
		/* 29 */ YY_NO_ANCHOR,
		/* 30 */ YY_NO_ANCHOR,
		/* 31 */ YY_NO_ANCHOR,
		/* 32 */ YY_NO_ANCHOR,
		/* 33 */ YY_NO_ANCHOR,
		/* 34 */ YY_NO_ANCHOR,
		/* 35 */ YY_NO_ANCHOR,
		/* 36 */ YY_NO_ANCHOR,
		/* 37 */ YY_NO_ANCHOR,
		/* 38 */ YY_NO_ANCHOR,
		/* 39 */ YY_NO_ANCHOR,
		/* 40 */ YY_NO_ANCHOR,
		/* 41 */ YY_NOT_ACCEPT,
		/* 42 */ YY_NO_ANCHOR,
		/* 43 */ YY_NO_ANCHOR,
		/* 44 */ YY_NO_ANCHOR,
		/* 45 */ YY_NOT_ACCEPT,
		/* 46 */ YY_NO_ANCHOR,
		/* 47 */ YY_NOT_ACCEPT,
		/* 48 */ YY_NO_ANCHOR,
		/* 49 */ YY_NO_ANCHOR,
		/* 50 */ YY_NO_ANCHOR,
		/* 51 */ YY_NO_ANCHOR,
		/* 52 */ YY_NO_ANCHOR,
		/* 53 */ YY_NO_ANCHOR,
		/* 54 */ YY_NO_ANCHOR,
		/* 55 */ YY_NO_ANCHOR,
		/* 56 */ YY_NO_ANCHOR,
		/* 57 */ YY_NO_ANCHOR,
		/* 58 */ YY_NO_ANCHOR
	};
	private int yy_cmap[] = unpackFromString(1,65538,
"2:8,39:2,3,2:2,3,2:18,39,13,36,25,2,19,2:2,17,18,16,15,20,35,33,1,32:10,37," +
"12,23,24,26,14,2,37:4,34,37:21,21,2,22,27,38,2,9,37:3,7,8,37:5,10,37:5,5,11" +
",4,6,37:5,30,29,31,28,2:65409,0:2")[0];

	private int yy_rmap[] = unpackFromString(1,59,
"0,1,2,1,3,1:7,4,1:3,5,6,1,7,1,8,1:3,9,10,11,1:6,12,13,4:2,14,15,16:2,17,18," +
"19,17,20,18,21,12,22,23,14,24,4,25,26,4,27")[0];

	private int yy_nxt[][] = unpackFromString(28,40,
"1,2,3,4,39,57:3,58,57:3,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23" +
",24,25,57:3,40,57:2,4,-1:41,26,-1:41,4,-1:35,4,-1:4,57:8,-1:7,54,-1:12,54:2" +
",57,54,-1,57,54,-1:25,27,-1:41,28,-1:37,29,-1:39,30,-1:19,57:3,44,57:4,-1:7" +
",54,-1:12,25,46,44,54,-1,57,54,-1:2,26:2,32,26:36,-1:26,33,-1:17,57:8,-1:7," +
"54,-1:12,34,54,57,54,-1,57,54,-1:5,57:3,51,57:4,-1:7,54,-1:12,35,54,51,54,-" +
"1,57,54,-1:5,57:8,-1:7,54,-1:12,38,54,57,54,-1,57,54,-1:5,57,53,57:6,-1:7,5" +
"4,-1:12,54:2,57,54,-1,57,54,-1:2,41:35,31,41:3,-1:32,42,-1:39,43,-1:11,57:8" +
",-1:3,45,-1:3,54,-1:12,34,54,57,49,-1,57,54,-1:5,57:8,-1:7,54,-1:12,35,54,5" +
"7,54,-1,57,54,-1:5,57:3,36,57:4,-1:7,54,-1:12,54:2,57,54,-1,57,54,-1:5,57:3" +
",37,57:4,-1:7,54,-1:12,54:2,57,54,-1,57,54,-1:5,57:8,-1:3,47,-1:3,54,-1:12," +
"38,54,57,52,-1,57,54,-1:5,57:2,48,57:5,-1:7,54,-1:12,54:2,57,54,-1,57,54,-1" +
":5,57:6,56,57,-1:7,54,-1:12,54:2,57,54,-1,57,54,-1:5,57:7,50,-1:7,54,-1:12," +
"54:2,57,54,-1,57,54,-1:5,57:5,55,57:2,-1:7,54,-1:12,54:2,57,54,-1,57,54,-1");

	public Symbol nextToken ()
		throws java.io.IOException {
		int yy_lookahead;
		int yy_anchor = YY_NO_ANCHOR;
		int yy_state = yy_state_dtrans[yy_lexical_state];
		int yy_next_state = YY_NO_STATE;
		int yy_last_accept_state = YY_NO_STATE;
		boolean yy_initial = true;
		int yy_this_accept;

		yy_mark_start();
		yy_this_accept = yy_acpt[yy_state];
		if (YY_NOT_ACCEPT != yy_this_accept) {
			yy_last_accept_state = yy_state;
			yy_mark_end();
		}
		while (true) {
			if (yy_initial && yy_at_bol) yy_lookahead = YY_BOL;
			else yy_lookahead = yy_advance();
			yy_next_state = YY_F;
			yy_next_state = yy_nxt[yy_rmap[yy_state]][yy_cmap[yy_lookahead]];
			if (YY_EOF == yy_lookahead && true == yy_initial) {
  
	return new Symbol(Symbol.EOF); 
			}
			if (YY_F != yy_next_state) {
				yy_state = yy_next_state;
				yy_initial = false;
				yy_this_accept = yy_acpt[yy_state];
				if (YY_NOT_ACCEPT != yy_this_accept) {
					yy_last_accept_state = yy_state;
					yy_mark_end();
				}
			}
			else {
				if (YY_NO_STATE == yy_last_accept_state) {
					throw (new Error("Lexical Error: Unmatched Input."));
				}
				else {
					yy_anchor = yy_acpt[yy_last_accept_state];
					if (0 != (YY_END & yy_anchor)) {
						yy_move_end();
					}
					yy_to_mark();
					switch (yy_last_accept_state) {
					case 0:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -2:
						break;
					case 1:
						
					case -3:
						break;
					case 2:
						{ return new Symbol(Symbol.DIV, null, yyline()); }
					case -4:
						break;
					case 3:
						{ System.err.println("Illegal character: "+yytext()+":ln:" + yyline()); System.exit(1); }
					case -5:
						break;
					case 4:
						{ /* ignore white space. */ }
					case -6:
						break;
					case 5:
						{ return new Symbol(Symbol.SEMI, null, yyline()); }
					case -7:
						break;
					case 6:
						{ return new Symbol(Symbol.BANG, null, yyline()); }
					case -8:
						break;
					case 7:
						{ return new Symbol(Symbol.QST, null, yyline()); }
					case -9:
						break;
					case 8:
						{ return new Symbol(Symbol.PLUS, null, yyline()); }
					case -10:
						break;
					case 9:
						{ return new Symbol(Symbol.TIMES, null, yyline()); }
					case -11:
						break;
					case 10:
						{ return new Symbol(Symbol.LPAREN, null, yyline()); }
					case -12:
						break;
					case 11:
						{ return new Symbol(Symbol.RPAREN, null, yyline()); }
					case -13:
						break;
					case 12:
						{ return new Symbol(Symbol.MOD, null, yyline()); }
					case -14:
						break;
					case 13:
						{ return new Symbol(Symbol.COMMA, null, yyline()); }
					case -15:
						break;
					case 14:
						{ return new Symbol(Symbol.LBRACK, null, yyline()); }
					case -16:
						break;
					case 15:
						{ return new Symbol(Symbol.RBRACK, null, yyline()); }
					case -17:
						break;
					case 16:
						{ return new Symbol(Symbol.LESS, null, yyline()); }
					case -18:
						break;
					case 17:
						{ return new Symbol(Symbol.EQUAL, null, yyline()); }
					case -19:
						break;
					case 18:
						{ return new Symbol(Symbol.COUNT, null, yyline()); }
					case -20:
						break;
					case 19:
						{ return new Symbol(Symbol.GREATER, null, yyline()); }
					case -21:
						break;
					case 20:
						{ return new Symbol(Symbol.AND, null, yyline()); }
					case -22:
						break;
					case 21:
						{ return new Symbol(Symbol.NOT, null, yyline()); }
					case -23:
						break;
					case 22:
						{ return new Symbol(Symbol.OR, null, yyline()); }
					case -24:
						break;
					case 23:
						{ return new Symbol(Symbol.LCBRACE, null, yyline()); }
					case -25:
						break;
					case 24:
						{ return new Symbol(Symbol.RCBRACE, null, yyline()); }
					case -26:
						break;
					case 25:
						{ return new Symbol(Symbol.INTEGER, new Integer(yytext()), yyline()); }
					case -27:
						break;
					case 27:
						{ return new Symbol(Symbol.LESSEQ, null, yyline()); }
					case -28:
						break;
					case 28:
						{ return new Symbol(Symbol.IMPLY, null, yyline()); }
					case -29:
						break;
					case 29:
						{ return new Symbol(Symbol.GREATEREQ, null, yyline()); }
					case -30:
						break;
					case 30:
						{ return new Symbol(Symbol.NEQUAL, null, yyline()); }
					case -31:
						break;
					case 31:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -32:
						break;
					case 32:
						{ return new Symbol(Symbol.COMMENT, yytext(), yyline()); }
					case -33:
						break;
					case 33:
						{ return new Symbol(Symbol.EQUIV, null, yyline()); }
					case -34:
						break;
					case 34:
						{ return new Symbol(Symbol.DOUBLE, new Double(yytext()), yyline()); }
					case -35:
						break;
					case 35:
						{ return new Symbol(Symbol.DOUBLE, new Double(yytext()), yyline()); }
					case -36:
						break;
					case 36:
						{ return new Symbol(Symbol.TRUE, null, yyline()); }
					case -37:
						break;
					case 37:
						{ return new Symbol(Symbol.FALSE, null, yyline()); }
					case -38:
						break;
					case 38:
						{ return new Symbol(Symbol.DOUBLE, new Double(yytext()), yyline()); }
					case -39:
						break;
					case 39:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -40:
						break;
					case 40:
						{ System.err.println("Illegal character: "+yytext()+":ln:" + yyline()); System.exit(1); }
					case -41:
						break;
					case 42:
						{ return new Symbol(Symbol.DOUBLE, new Double(yytext()), yyline()); }
					case -42:
						break;
					case 43:
						{ return new Symbol(Symbol.DOUBLE, new Double(yytext()), yyline()); }
					case -43:
						break;
					case 44:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -44:
						break;
					case 46:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -45:
						break;
					case 48:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -46:
						break;
					case 49:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -47:
						break;
					case 50:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -48:
						break;
					case 51:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -49:
						break;
					case 52:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -50:
						break;
					case 53:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -51:
						break;
					case 54:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -52:
						break;
					case 55:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -53:
						break;
					case 56:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -54:
						break;
					case 57:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -55:
						break;
					case 58:
						{ return new Symbol(Symbol.IDENT, yytext(), yyline()); }
					case -56:
						break;
					default:
						yy_error(YY_E_INTERNAL,false);
					case -1:
					}
					yy_initial = true;
					yy_state = yy_state_dtrans[yy_lexical_state];
					yy_next_state = YY_NO_STATE;
					yy_last_accept_state = YY_NO_STATE;
					yy_mark_start();
					yy_this_accept = yy_acpt[yy_state];
					if (YY_NOT_ACCEPT != yy_this_accept) {
						yy_last_accept_state = yy_state;
						yy_mark_end();
					}
				}
			}
		}
	}
}
