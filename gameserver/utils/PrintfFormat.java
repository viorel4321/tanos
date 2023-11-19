package l2s.gameserver.utils;

import java.text.DecimalFormatSymbols;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

public class PrintfFormat
{
	public static final PrintfFormat LOG_BOSS_KILLED;
	public static final PrintfFormat LOG_BOSS_RESPAWN;
	private Vector<ConversionSpecification> vFmt;
	private int cPos;
	private DecimalFormatSymbols dfs;

	public PrintfFormat(final String fmtArg) throws IllegalArgumentException
	{
		this(Locale.getDefault(), fmtArg);
	}

	public PrintfFormat(final Locale locale, final String fmtArg) throws IllegalArgumentException
	{
		vFmt = new Vector<ConversionSpecification>();
		cPos = 0;
		dfs = null;
		dfs = new DecimalFormatSymbols(locale);
		int ePos = 0;
		ConversionSpecification sFmt = null;
		String unCS = nonControl(fmtArg, 0);
		if(unCS != null)
		{
			sFmt = new ConversionSpecification();
			sFmt.setLiteral(unCS);
			vFmt.addElement(sFmt);
		}
		while(cPos != -1 && cPos < fmtArg.length())
		{
			for(ePos = cPos + 1; ePos < fmtArg.length(); ++ePos)
			{
				char c = '\0';
				c = fmtArg.charAt(ePos);
				if(c == 'i')
					break;
				if(c == 'd')
					break;
				if(c == 'f')
					break;
				if(c == 'g')
					break;
				if(c == 'G')
					break;
				if(c == 'o')
					break;
				if(c == 'x')
					break;
				if(c == 'X')
					break;
				if(c == 'e')
					break;
				if(c == 'E')
					break;
				if(c == 'c')
					break;
				if(c == 's')
					break;
				if(c == '%')
					break;
			}
			ePos = Math.min(ePos + 1, fmtArg.length());
			sFmt = new ConversionSpecification(fmtArg.substring(cPos, ePos));
			vFmt.addElement(sFmt);
			unCS = nonControl(fmtArg, ePos);
			if(unCS != null)
			{
				sFmt = new ConversionSpecification();
				sFmt.setLiteral(unCS);
				vFmt.addElement(sFmt);
			}
		}
	}

	private String nonControl(final String s, final int start)
	{
		cPos = s.indexOf("%", start);
		if(cPos == -1)
			cPos = s.length();
		return s.substring(start, cPos);
	}

	public String sprintf(final Object[] o)
	{
		final Enumeration<?> e = vFmt.elements();
		ConversionSpecification cs = null;
		char c = '\0';
		int i = 0;
		final StringBuffer sb = new StringBuffer();
		while(e.hasMoreElements())
		{
			cs = (ConversionSpecification) e.nextElement();
			c = cs.getConversionCharacter();
			if(c == '\0')
				sb.append(cs.getLiteral());
			else if(c == '%')
				sb.append("%");
			else
			{
				if(cs.isPositionalSpecification())
				{
					i = cs.getArgumentPosition() - 1;
					if(cs.isPositionalFieldWidth())
					{
						final int ifw = cs.getArgumentPositionForFieldWidth() - 1;
						cs.setFieldWidthWithArg((int) o[ifw]);
					}
					if(cs.isPositionalPrecision())
					{
						final int ipr = cs.getArgumentPositionForPrecision() - 1;
						cs.setPrecisionWithArg((int) o[ipr]);
					}
				}
				else
				{
					if(cs.isVariableFieldWidth())
					{
						cs.setFieldWidthWithArg((int) o[i]);
						++i;
					}
					if(cs.isVariablePrecision())
					{
						cs.setPrecisionWithArg((int) o[i]);
						++i;
					}
				}
				if(o[i] instanceof Byte)
					sb.append(cs.internalsprintf((byte) o[i]));
				else if(o[i] instanceof Short)
					sb.append(cs.internalsprintf((short) o[i]));
				else if(o[i] instanceof Integer)
					sb.append(cs.internalsprintf((int) o[i]));
				else if(o[i] instanceof Long)
					sb.append(cs.internalsprintf((long) o[i]));
				else if(o[i] instanceof Float)
					sb.append(cs.internalsprintf((float) o[i]));
				else if(o[i] instanceof Double)
					sb.append(cs.internalsprintf((double) o[i]));
				else if(o[i] instanceof Character)
					sb.append(cs.internalsprintf((char) o[i]));
				else if(o[i] instanceof String)
					sb.append(cs.internalsprintf((String) o[i]));
				else
					sb.append(cs.internalsprintf(o[i]));
				if(cs.isPositionalSpecification())
					continue;
				++i;
			}
		}
		return sb.toString();
	}

	public String sprintf()
	{
		final Enumeration<?> e = vFmt.elements();
		ConversionSpecification cs = null;
		char c = '\0';
		final StringBuffer sb = new StringBuffer();
		while(e.hasMoreElements())
		{
			cs = (ConversionSpecification) e.nextElement();
			c = cs.getConversionCharacter();
			if(c == '\0')
				sb.append(cs.getLiteral());
			else
			{
				if(c != '%')
					continue;
				sb.append("%");
			}
		}
		return sb.toString();
	}

	public String sprintf(final int x) throws IllegalArgumentException
	{
		final Enumeration<?> e = vFmt.elements();
		ConversionSpecification cs = null;
		char c = '\0';
		final StringBuffer sb = new StringBuffer();
		while(e.hasMoreElements())
		{
			cs = (ConversionSpecification) e.nextElement();
			c = cs.getConversionCharacter();
			if(c == '\0')
				sb.append(cs.getLiteral());
			else if(c == '%')
				sb.append("%");
			else
				sb.append(cs.internalsprintf(x));
		}
		return sb.toString();
	}

	public String sprintf(final long x) throws IllegalArgumentException
	{
		final Enumeration<?> e = vFmt.elements();
		ConversionSpecification cs = null;
		char c = '\0';
		final StringBuffer sb = new StringBuffer();
		while(e.hasMoreElements())
		{
			cs = (ConversionSpecification) e.nextElement();
			c = cs.getConversionCharacter();
			if(c == '\0')
				sb.append(cs.getLiteral());
			else if(c == '%')
				sb.append("%");
			else
				sb.append(cs.internalsprintf(x));
		}
		return sb.toString();
	}

	public String sprintf(final double x) throws IllegalArgumentException
	{
		final Enumeration<?> e = vFmt.elements();
		ConversionSpecification cs = null;
		char c = '\0';
		final StringBuffer sb = new StringBuffer();
		while(e.hasMoreElements())
		{
			cs = (ConversionSpecification) e.nextElement();
			c = cs.getConversionCharacter();
			if(c == '\0')
				sb.append(cs.getLiteral());
			else if(c == '%')
				sb.append("%");
			else
				sb.append(cs.internalsprintf(x));
		}
		return sb.toString();
	}

	public String sprintf(final String x) throws IllegalArgumentException
	{
		final Enumeration<?> e = vFmt.elements();
		ConversionSpecification cs = null;
		char c = '\0';
		final StringBuffer sb = new StringBuffer();
		while(e.hasMoreElements())
		{
			cs = (ConversionSpecification) e.nextElement();
			c = cs.getConversionCharacter();
			if(c == '\0')
				sb.append(cs.getLiteral());
			else if(c == '%')
				sb.append("%");
			else
				sb.append(cs.internalsprintf(x));
		}
		return sb.toString();
	}

	public String sprintf(final Object x) throws IllegalArgumentException
	{
		final Enumeration<?> e = vFmt.elements();
		ConversionSpecification cs = null;
		char c = '\0';
		final StringBuffer sb = new StringBuffer();
		while(e.hasMoreElements())
		{
			cs = (ConversionSpecification) e.nextElement();
			c = cs.getConversionCharacter();
			if(c == '\0')
				sb.append(cs.getLiteral());
			else if(c == '%')
				sb.append("%");
			else if(x instanceof Byte)
				sb.append(cs.internalsprintf((byte) x));
			else if(x instanceof Short)
				sb.append(cs.internalsprintf((short) x));
			else if(x instanceof Integer)
				sb.append(cs.internalsprintf((int) x));
			else if(x instanceof Long)
				sb.append(cs.internalsprintf((long) x));
			else if(x instanceof Float)
				sb.append(cs.internalsprintf((float) x));
			else if(x instanceof Double)
				sb.append(cs.internalsprintf((double) x));
			else if(x instanceof Character)
				sb.append(cs.internalsprintf((char) x));
			else if(x instanceof String)
				sb.append(cs.internalsprintf((String) x));
			else
				sb.append(cs.internalsprintf(x));
		}
		return sb.toString();
	}

	static
	{
		LOG_BOSS_KILLED = new PrintfFormat("%s: %s[%d] killed by %s at Loc(%d %d %d) in %s");
		LOG_BOSS_RESPAWN = new PrintfFormat("%s: %s[%d] scheduled for respawn in %s at %s");
	}

	private class ConversionSpecification
	{
		private boolean thousands;
		private boolean leftJustify;
		private boolean leadingSign;
		private boolean leadingSpace;
		private boolean alternateForm;
		private boolean leadingZeros;
		private boolean variableFieldWidth;
		private int fieldWidth;
		private boolean fieldWidthSet;
		private int precision;
		private static final int defaultDigits = 6;
		private boolean variablePrecision;
		private boolean precisionSet;
		private boolean positionalSpecification;
		private int argumentPosition;
		private boolean positionalFieldWidth;
		private int argumentPositionForFieldWidth;
		private boolean positionalPrecision;
		private int argumentPositionForPrecision;
		private boolean optionalh;
		private boolean optionall;
		private boolean optionalL;
		private char conversionCharacter;
		private int pos;
		private String fmt;

		ConversionSpecification()
		{
			thousands = false;
			leftJustify = false;
			leadingSign = false;
			leadingSpace = false;
			alternateForm = false;
			leadingZeros = false;
			variableFieldWidth = false;
			fieldWidth = 0;
			fieldWidthSet = false;
			precision = 0;
			variablePrecision = false;
			precisionSet = false;
			positionalSpecification = false;
			argumentPosition = 0;
			positionalFieldWidth = false;
			argumentPositionForFieldWidth = 0;
			positionalPrecision = false;
			argumentPositionForPrecision = 0;
			optionalh = false;
			optionall = false;
			optionalL = false;
			conversionCharacter = '\0';
			pos = 0;
		}

		ConversionSpecification(final String fmtArg) throws IllegalArgumentException
		{
			thousands = false;
			leftJustify = false;
			leadingSign = false;
			leadingSpace = false;
			alternateForm = false;
			leadingZeros = false;
			variableFieldWidth = false;
			fieldWidth = 0;
			fieldWidthSet = false;
			precision = 0;
			variablePrecision = false;
			precisionSet = false;
			positionalSpecification = false;
			argumentPosition = 0;
			positionalFieldWidth = false;
			argumentPositionForFieldWidth = 0;
			positionalPrecision = false;
			argumentPositionForPrecision = 0;
			optionalh = false;
			optionall = false;
			optionalL = false;
			conversionCharacter = '\0';
			pos = 0;
			if(fmtArg == null)
				throw new NullPointerException();
			if(fmtArg.length() == 0)
				throw new IllegalArgumentException("Control strings must have positive lengths.");
			if(fmtArg.charAt(0) != '%')
				throw new IllegalArgumentException("Control strings must begin with %.");
			fmt = fmtArg;
			pos = 1;
			setArgPosition();
			setFlagCharacters();
			setFieldWidth();
			setPrecision();
			setOptionalHL();
			if(!setConversionCharacter())
				throw new IllegalArgumentException("Malformed conversion specification=" + fmtArg);
			if(pos == fmtArg.length())
			{
				if(leadingZeros && leftJustify)
					leadingZeros = false;
				if(precisionSet && leadingZeros && (conversionCharacter == 'd' || conversionCharacter == 'i' || conversionCharacter == 'o' || conversionCharacter == 'x'))
					leadingZeros = false;
				return;
			}
			throw new IllegalArgumentException("Malformed conversion specification=" + fmtArg);
		}

		void setLiteral(final String s)
		{
			fmt = s;
		}

		String getLiteral()
		{
			final StringBuffer sb = new StringBuffer();
			int i = 0;
			while(i < fmt.length())
				if(fmt.charAt(i) == '\\')
				{
					if(++i < fmt.length())
					{
						final char c = fmt.charAt(i);
						switch(c)
						{
							case 'a':
							{
								sb.append('\u0007');
								break;
							}
							case 'b':
							{
								sb.append('\b');
								break;
							}
							case 'f':
							{
								sb.append('\f');
								break;
							}
							case 'n':
							{
								sb.append(System.getProperty("line.separator"));
								break;
							}
							case 'r':
							{
								sb.append('\r');
								break;
							}
							case 't':
							{
								sb.append('\t');
								break;
							}
							case 'v':
							{
								sb.append('\u000b');
								break;
							}
							case '\\':
							{
								sb.append('\\');
								break;
							}
						}
						++i;
					}
					else
						sb.append('\\');
				}
				else
					++i;
			return fmt;
		}

		char getConversionCharacter()
		{
			return conversionCharacter;
		}

		boolean isVariableFieldWidth()
		{
			return variableFieldWidth;
		}

		void setFieldWidthWithArg(final int fw)
		{
			if(fw < 0)
				leftJustify = true;
			fieldWidthSet = true;
			fieldWidth = Math.abs(fw);
		}

		boolean isVariablePrecision()
		{
			return variablePrecision;
		}

		void setPrecisionWithArg(final int pr)
		{
			precisionSet = true;
			precision = Math.max(pr, 0);
		}

		String internalsprintf(final int s) throws IllegalArgumentException
		{
			String s2 = "";
			switch(conversionCharacter)
			{
				case 'd':
				case 'i':
				{
					if(optionalh)
					{
						s2 = this.printDFormat((short) s);
						break;
					}
					if(optionall)
					{
						s2 = this.printDFormat((long) s);
						break;
					}
					s2 = this.printDFormat(s);
					break;
				}
				case 'X':
				case 'x':
				{
					if(optionalh)
					{
						s2 = this.printXFormat((short) s);
						break;
					}
					if(optionall)
					{
						s2 = this.printXFormat((long) s);
						break;
					}
					s2 = this.printXFormat(s);
					break;
				}
				case 'o':
				{
					if(optionalh)
					{
						s2 = this.printOFormat((short) s);
						break;
					}
					if(optionall)
					{
						s2 = this.printOFormat((long) s);
						break;
					}
					s2 = this.printOFormat(s);
					break;
				}
				case 'C':
				case 'c':
				{
					s2 = printCFormat((char) s);
					break;
				}
				default:
				{
					throw new IllegalArgumentException("Cannot format a int with a format using a " + conversionCharacter + " conversion character.");
				}
			}
			return s2;
		}

		String internalsprintf(final long s) throws IllegalArgumentException
		{
			String s2 = "";
			switch(conversionCharacter)
			{
				case 'd':
				case 'i':
				{
					if(optionalh)
					{
						s2 = this.printDFormat((short) s);
						break;
					}
					if(optionall)
					{
						s2 = this.printDFormat(s);
						break;
					}
					s2 = this.printDFormat((int) s);
					break;
				}
				case 'X':
				case 'x':
				{
					if(optionalh)
					{
						s2 = this.printXFormat((short) s);
						break;
					}
					if(optionall)
					{
						s2 = this.printXFormat(s);
						break;
					}
					s2 = this.printXFormat((int) s);
					break;
				}
				case 'o':
				{
					if(optionalh)
					{
						s2 = this.printOFormat((short) s);
						break;
					}
					if(optionall)
					{
						s2 = this.printOFormat(s);
						break;
					}
					s2 = this.printOFormat((int) s);
					break;
				}
				case 'C':
				case 'c':
				{
					s2 = printCFormat((char) s);
					break;
				}
				default:
				{
					throw new IllegalArgumentException("Cannot format a long with a format using a " + conversionCharacter + " conversion character.");
				}
			}
			return s2;
		}

		String internalsprintf(final double s) throws IllegalArgumentException
		{
			String s2 = "";
			switch(conversionCharacter)
			{
				case 'f':
				{
					s2 = printFFormat(s);
					break;
				}
				case 'E':
				case 'e':
				{
					s2 = printEFormat(s);
					break;
				}
				case 'G':
				case 'g':
				{
					s2 = printGFormat(s);
					break;
				}
				default:
				{
					throw new IllegalArgumentException("Cannot format a double with a format using a " + conversionCharacter + " conversion character.");
				}
			}
			return s2;
		}

		String internalsprintf(final String s) throws IllegalArgumentException
		{
			String s2 = "";
			if(conversionCharacter == 's' || conversionCharacter == 'S')
			{
				s2 = printSFormat(s);
				return s2;
			}
			throw new IllegalArgumentException("Cannot format a String with a format using a " + conversionCharacter + " conversion character.");
		}

		String internalsprintf(final Object s)
		{
			String s2 = "";
			if(conversionCharacter == 's' || conversionCharacter == 'S')
			{
				s2 = printSFormat(s.toString());
				return s2;
			}
			throw new IllegalArgumentException("Cannot format a String with a format using a " + conversionCharacter + " conversion character.");
		}

		private char[] fFormatDigits(final double x)
		{
			int expon = 0;
			boolean minusSign = false;
			String sx;
			if(x > 0.0)
				sx = Double.toString(x);
			else if(x < 0.0)
			{
				sx = Double.toString(-x);
				minusSign = true;
			}
			else
			{
				sx = Double.toString(x);
				if(sx.charAt(0) == '-')
				{
					minusSign = true;
					sx = sx.substring(1);
				}
			}
			final int ePos = sx.indexOf(69);
			final int rPos = sx.indexOf(46);
			int n1In;
			if(rPos != -1)
				n1In = rPos;
			else if(ePos != -1)
				n1In = ePos;
			else
				n1In = sx.length();
			int n2In;
			if(rPos != -1)
			{
				if(ePos != -1)
					n2In = ePos - rPos - 1;
				else
					n2In = sx.length() - rPos - 1;
			}
			else
				n2In = 0;
			if(ePos != -1)
			{
				int ie = ePos + 1;
				expon = 0;
				if(sx.charAt(ie) == '-')
				{
					++ie;
					while(ie < sx.length() && sx.charAt(ie) == '0')
						++ie;
					if(ie < sx.length())
						expon = -Integer.parseInt(sx.substring(ie));
				}
				else
				{
					if(sx.charAt(ie) == '+')
						++ie;
					while(ie < sx.length() && sx.charAt(ie) == '0')
						++ie;
					if(ie < sx.length())
						expon = Integer.parseInt(sx.substring(ie));
				}
			}
			int p;
			if(precisionSet)
				p = precision;
			else
				p = 5;
			final char[] ca1 = sx.toCharArray();
			final char[] ca2 = new char[n1In + n2In];
			int j;
			for(j = 0; j < n1In; ++j)
				ca2[j] = ca1[j];
			int i = j + 1;
			for(int k = 0; k < n2In; ++k)
			{
				ca2[j] = ca1[i];
				++j;
				++i;
			}
			char[] ca3;
			if(n1In + expon <= 0)
			{
				ca3 = new char[-expon + n2In];
				j = 0;
				for(int k = 0; k < -n1In - expon; ++k, ++j)
					ca3[j] = '0';
				for(i = 0; i < n1In + n2In; ++i, ++j)
					ca3[j] = ca2[i];
			}
			else
				ca3 = ca2;
			boolean carry = false;
			if(p < -expon + n2In)
			{
				if(expon < 0)
					i = p;
				else
					i = p + n1In;
				carry = checkForCarry(ca3, i);
				if(carry)
					carry = startSymbolicCarry(ca3, i - 1, 0);
			}
			char[] ca4;
			if(n1In + expon <= 0)
			{
				ca4 = new char[2 + p];
				if(!carry)
					ca4[0] = '0';
				else
					ca4[0] = '1';
				if(alternateForm || !precisionSet || precision != 0)
				{
					ca4[1] = '.';
					for(i = 0, j = 2; i < Math.min(p, ca3.length); ++i, ++j)
						ca4[j] = ca3[i];
					while(j < ca4.length)
					{
						ca4[j] = '0';
						++j;
					}
				}
			}
			else
			{
				if(!carry)
				{
					if(alternateForm || !precisionSet || precision != 0)
						ca4 = new char[n1In + expon + p + 1];
					else
						ca4 = new char[n1In + expon];
					j = 0;
				}
				else
				{
					if(alternateForm || !precisionSet || precision != 0)
						ca4 = new char[n1In + expon + p + 2];
					else
						ca4 = new char[n1In + expon + 1];
					ca4[0] = '1';
					j = 1;
				}
				for(i = 0; i < Math.min(n1In + expon, ca3.length); ++i, ++j)
					ca4[j] = ca3[i];
				while(i < n1In + expon)
				{
					ca4[j] = '0';
					++i;
					++j;
				}
				if(alternateForm || !precisionSet || precision != 0)
				{
					ca4[j] = '.';
					++j;
					for(int k = 0; i < ca3.length && k < p; ++i, ++j, ++k)
						ca4[j] = ca3[i];
					while(j < ca4.length)
					{
						ca4[j] = '0';
						++j;
					}
				}
			}
			int nZeros = 0;
			if(!leftJustify && leadingZeros)
			{
				int xThousands = 0;
				if(thousands)
				{
					int xlead = 0;
					if(ca4[0] == '+' || ca4[0] == '-' || ca4[0] == ' ')
						xlead = 1;
					int xdp;
					for(xdp = xlead; xdp < ca4.length && ca4[xdp] != '.'; ++xdp)
					{}
					xThousands = (xdp - xlead) / 3;
				}
				if(fieldWidthSet)
					nZeros = fieldWidth - ca4.length;
				if(!minusSign && (leadingSign || leadingSpace) || minusSign)
					--nZeros;
				nZeros -= xThousands;
				if(nZeros < 0)
					nZeros = 0;
			}
			j = 0;
			char[] ca5;
			if(!minusSign && (leadingSign || leadingSpace) || minusSign)
			{
				ca5 = new char[ca4.length + nZeros + 1];
				++j;
			}
			else
				ca5 = new char[ca4.length + nZeros];
			if(!minusSign)
			{
				if(leadingSign)
					ca5[0] = '+';
				if(leadingSpace)
					ca5[0] = ' ';
			}
			else
				ca5[0] = '-';
			for(i = 0; i < nZeros; ++i, ++j)
				ca5[j] = '0';
			for(i = 0; i < ca4.length; ++i, ++j)
				ca5[j] = ca4[i];
			int lead = 0;
			if(ca5[0] == '+' || ca5[0] == '-' || ca5[0] == ' ')
				lead = 1;
			int dp;
			for(dp = lead; dp < ca5.length && ca5[dp] != '.'; ++dp)
			{}
			final int nThousands = (dp - lead) / 3;
			if(dp < ca5.length)
				ca5[dp] = dfs.getDecimalSeparator();
			char[] ca6 = ca5;
			if(thousands && nThousands > 0)
			{
				ca6 = new char[ca5.length + nThousands + lead];
				ca6[0] = ca5[0];
				i = lead;
				int k = lead;
				while(i < dp)
				{
					if(i > 0 && (dp - i) % 3 == 0)
					{
						ca6[k] = dfs.getGroupingSeparator();
						ca6[k + 1] = ca5[i];
						k += 2;
					}
					else
					{
						ca6[k] = ca5[i];
						++k;
					}
					++i;
				}
				while(i < ca5.length)
				{
					ca6[k] = ca5[i];
					++i;
					++k;
				}
			}
			return ca6;
		}

		private String fFormatString(final double x)
		{
			char[] ca6;
			if(Double.isInfinite(x))
			{
				if(x == Double.POSITIVE_INFINITY)
				{
					if(leadingSign)
						ca6 = "+Inf".toCharArray();
					else if(leadingSpace)
						ca6 = " Inf".toCharArray();
					else
						ca6 = "Inf".toCharArray();
				}
				else
					ca6 = "-Inf".toCharArray();
			}
			else if(Double.isNaN(x))
			{
				if(leadingSign)
					ca6 = "+NaN".toCharArray();
				else if(leadingSpace)
					ca6 = " NaN".toCharArray();
				else
					ca6 = "NaN".toCharArray();
			}
			else
				ca6 = fFormatDigits(x);
			final char[] ca7 = applyFloatPadding(ca6, false);
			return new String(ca7);
		}

		private char[] eFormatDigits(final double x, final char eChar)
		{
			int expon = 0;
			boolean minusSign = false;
			String sx;
			if(x > 0.0)
				sx = Double.toString(x);
			else if(x < 0.0)
			{
				sx = Double.toString(-x);
				minusSign = true;
			}
			else
			{
				sx = Double.toString(x);
				if(sx.charAt(0) == '-')
				{
					minusSign = true;
					sx = sx.substring(1);
				}
			}
			int ePos = sx.indexOf(69);
			if(ePos == -1)
				ePos = sx.indexOf(101);
			final int rPos = sx.indexOf(46);
			if(ePos != -1)
			{
				int ie = ePos + 1;
				expon = 0;
				if(sx.charAt(ie) == '-')
				{
					++ie;
					while(ie < sx.length() && sx.charAt(ie) == '0')
						++ie;
					if(ie < sx.length())
						expon = -Integer.parseInt(sx.substring(ie));
				}
				else
				{
					if(sx.charAt(ie) == '+')
						++ie;
					while(ie < sx.length() && sx.charAt(ie) == '0')
						++ie;
					if(ie < sx.length())
						expon = Integer.parseInt(sx.substring(ie));
				}
			}
			if(rPos != -1)
				expon += rPos - 1;
			int p;
			if(precisionSet)
				p = precision;
			else
				p = 5;
			char[] ca1;
			if(rPos != -1 && ePos != -1)
				ca1 = (sx.substring(0, rPos) + sx.substring(rPos + 1, ePos)).toCharArray();
			else if(rPos != -1)
				ca1 = (sx.substring(0, rPos) + sx.substring(rPos + 1)).toCharArray();
			else if(ePos != -1)
				ca1 = sx.substring(0, ePos).toCharArray();
			else
				ca1 = sx.toCharArray();
			boolean carry = false;
			int i0 = 0;
			if(ca1[0] != '0')
				i0 = 0;
			else
				for(i0 = 0; i0 < ca1.length; ++i0)
					if(ca1[i0] != '0')
						break;
			if(i0 + p < ca1.length - 1)
			{
				carry = checkForCarry(ca1, i0 + p + 1);
				if(carry)
					carry = startSymbolicCarry(ca1, i0 + p, i0);
				if(carry)
				{
					final char[] ca2 = new char[i0 + p + 1];
					ca2[i0] = '1';
					for(int j = 0; j < i0; ++j)
						ca2[j] = '0';
					int k = i0;
					for(int j = i0 + 1; j < p + 1; ++j)
					{
						ca2[j] = ca1[k];
						++k;
					}
					++expon;
					ca1 = ca2;
				}
			}
			int eSize;
			if(Math.abs(expon) < 100 && !optionalL)
				eSize = 4;
			else
				eSize = 5;
			char[] ca2;
			if(alternateForm || !precisionSet || precision != 0)
				ca2 = new char[2 + p + eSize];
			else
				ca2 = new char[1 + eSize];
			int j;
			if(ca1[0] != '0')
			{
				ca2[0] = ca1[0];
				j = 1;
			}
			else
			{
				for(j = 1; j < (ePos == -1 ? ca1.length : ePos) && ca1[j] == '0'; ++j)
				{}
				if(ePos != -1 && j < ePos || ePos == -1 && j < ca1.length)
				{
					ca2[0] = ca1[j];
					expon -= j;
					++j;
				}
				else
				{
					ca2[0] = '0';
					j = 2;
				}
			}
			int k;
			if(alternateForm || !precisionSet || precision != 0)
			{
				ca2[1] = '.';
				k = 2;
			}
			else
				k = 1;
			for(int l = 0; l < p && j < ca1.length; ++j, ++k, ++l)
				ca2[k] = ca1[j];
			while(k < ca2.length - eSize)
			{
				ca2[k] = '0';
				++k;
			}
			ca2[k++] = eChar;
			if(expon < 0)
				ca2[k++] = '-';
			else
				ca2[k++] = '+';
			expon = Math.abs(expon);
			if(expon >= 100)
			{
				switch(expon / 100)
				{
					case 1:
					{
						ca2[k] = '1';
						break;
					}
					case 2:
					{
						ca2[k] = '2';
						break;
					}
					case 3:
					{
						ca2[k] = '3';
						break;
					}
					case 4:
					{
						ca2[k] = '4';
						break;
					}
					case 5:
					{
						ca2[k] = '5';
						break;
					}
					case 6:
					{
						ca2[k] = '6';
						break;
					}
					case 7:
					{
						ca2[k] = '7';
						break;
					}
					case 8:
					{
						ca2[k] = '8';
						break;
					}
					case 9:
					{
						ca2[k] = '9';
						break;
					}
				}
				++k;
			}
			switch(expon % 100 / 10)
			{
				case 0:
				{
					ca2[k] = '0';
					break;
				}
				case 1:
				{
					ca2[k] = '1';
					break;
				}
				case 2:
				{
					ca2[k] = '2';
					break;
				}
				case 3:
				{
					ca2[k] = '3';
					break;
				}
				case 4:
				{
					ca2[k] = '4';
					break;
				}
				case 5:
				{
					ca2[k] = '5';
					break;
				}
				case 6:
				{
					ca2[k] = '6';
					break;
				}
				case 7:
				{
					ca2[k] = '7';
					break;
				}
				case 8:
				{
					ca2[k] = '8';
					break;
				}
				case 9:
				{
					ca2[k] = '9';
					break;
				}
			}
			++k;
			switch(expon % 10)
			{
				case 0:
				{
					ca2[k] = '0';
					break;
				}
				case 1:
				{
					ca2[k] = '1';
					break;
				}
				case 2:
				{
					ca2[k] = '2';
					break;
				}
				case 3:
				{
					ca2[k] = '3';
					break;
				}
				case 4:
				{
					ca2[k] = '4';
					break;
				}
				case 5:
				{
					ca2[k] = '5';
					break;
				}
				case 6:
				{
					ca2[k] = '6';
					break;
				}
				case 7:
				{
					ca2[k] = '7';
					break;
				}
				case 8:
				{
					ca2[k] = '8';
					break;
				}
				case 9:
				{
					ca2[k] = '9';
					break;
				}
			}
			int nZeros = 0;
			if(!leftJustify && leadingZeros)
			{
				int xThousands = 0;
				if(thousands)
				{
					int xlead = 0;
					if(ca2[0] == '+' || ca2[0] == '-' || ca2[0] == ' ')
						xlead = 1;
					int xdp;
					for(xdp = xlead; xdp < ca2.length && ca2[xdp] != '.'; ++xdp)
					{}
					xThousands = (xdp - xlead) / 3;
				}
				if(fieldWidthSet)
					nZeros = fieldWidth - ca2.length;
				if(!minusSign && (leadingSign || leadingSpace) || minusSign)
					--nZeros;
				nZeros -= xThousands;
				if(nZeros < 0)
					nZeros = 0;
			}
			j = 0;
			char[] ca3;
			if(!minusSign && (leadingSign || leadingSpace) || minusSign)
			{
				ca3 = new char[ca2.length + nZeros + 1];
				++j;
			}
			else
				ca3 = new char[ca2.length + nZeros];
			if(!minusSign)
			{
				if(leadingSign)
					ca3[0] = '+';
				if(leadingSpace)
					ca3[0] = ' ';
			}
			else
				ca3[0] = '-';
			for(int l = 0; l < nZeros; ++l)
			{
				ca3[j] = '0';
				++j;
			}
			for(k = 0; k < ca2.length && j < ca3.length; ++k, ++j)
				ca3[j] = ca2[k];
			int lead = 0;
			if(ca3[0] == '+' || ca3[0] == '-' || ca3[0] == ' ')
				lead = 1;
			int dp;
			for(dp = lead; dp < ca3.length && ca3[dp] != '.'; ++dp)
			{}
			final int nThousands = dp / 3;
			if(dp < ca3.length)
				ca3[dp] = dfs.getDecimalSeparator();
			char[] ca4 = ca3;
			if(thousands && nThousands > 0)
			{
				ca4 = new char[ca3.length + nThousands + lead];
				ca4[0] = ca3[0];
				k = lead;
				int l = lead;
				while(k < dp)
				{
					if(k > 0 && (dp - k) % 3 == 0)
					{
						ca4[l] = dfs.getGroupingSeparator();
						ca4[l + 1] = ca3[k];
						l += 2;
					}
					else
					{
						ca4[l] = ca3[k];
						++l;
					}
					++k;
				}
				while(k < ca3.length)
				{
					ca4[l] = ca3[k];
					++k;
					++l;
				}
			}
			return ca4;
		}

		private boolean checkForCarry(final char[] ca1, final int icarry)
		{
			boolean carry = false;
			if(icarry < ca1.length)
				if(ca1[icarry] == '6' || ca1[icarry] == '7' || ca1[icarry] == '8' || ca1[icarry] == '9')
					carry = true;
				else if(ca1[icarry] == '5')
				{
					int ii;
					for(ii = icarry + 1; ii < ca1.length && ca1[ii] == '0'; ++ii)
					{}
					carry = ii < ca1.length;
					if(!carry && icarry > 0)
						carry = ca1[icarry - 1] == '1' || ca1[icarry - 1] == '3' || ca1[icarry - 1] == '5' || ca1[icarry - 1] == '7' || ca1[icarry - 1] == '9';
				}
			return carry;
		}

		private boolean startSymbolicCarry(final char[] ca, final int cLast, final int cFirst)
		{
			boolean carry = true;
			for(int i = cLast; carry && i >= cFirst; --i)
			{
				carry = false;
				switch(ca[i])
				{
					case '0':
					{
						ca[i] = '1';
						break;
					}
					case '1':
					{
						ca[i] = '2';
						break;
					}
					case '2':
					{
						ca[i] = '3';
						break;
					}
					case '3':
					{
						ca[i] = '4';
						break;
					}
					case '4':
					{
						ca[i] = '5';
						break;
					}
					case '5':
					{
						ca[i] = '6';
						break;
					}
					case '6':
					{
						ca[i] = '7';
						break;
					}
					case '7':
					{
						ca[i] = '8';
						break;
					}
					case '8':
					{
						ca[i] = '9';
						break;
					}
					case '9':
					{
						ca[i] = '0';
						carry = true;
						break;
					}
				}
			}
			return carry;
		}

		private String eFormatString(final double x, final char eChar)
		{
			char[] ca4;
			if(Double.isInfinite(x))
			{
				if(x == Double.POSITIVE_INFINITY)
				{
					if(leadingSign)
						ca4 = "+Inf".toCharArray();
					else if(leadingSpace)
						ca4 = " Inf".toCharArray();
					else
						ca4 = "Inf".toCharArray();
				}
				else
					ca4 = "-Inf".toCharArray();
			}
			else if(Double.isNaN(x))
			{
				if(leadingSign)
					ca4 = "+NaN".toCharArray();
				else if(leadingSpace)
					ca4 = " NaN".toCharArray();
				else
					ca4 = "NaN".toCharArray();
			}
			else
				ca4 = eFormatDigits(x, eChar);
			final char[] ca5 = applyFloatPadding(ca4, false);
			return new String(ca5);
		}

		private char[] applyFloatPadding(final char[] ca4, final boolean noDigits)
		{
			char[] ca5 = ca4;
			if(fieldWidthSet)
				if(leftJustify)
				{
					final int nBlanks = fieldWidth - ca4.length;
					if(nBlanks > 0)
					{
						ca5 = new char[ca4.length + nBlanks];
						int i;
						for(i = 0; i < ca4.length; ++i)
							ca5[i] = ca4[i];
						for(int j = 0; j < nBlanks; ++j, ++i)
							ca5[i] = ' ';
					}
				}
				else if(!leadingZeros || noDigits)
				{
					final int nBlanks = fieldWidth - ca4.length;
					if(nBlanks > 0)
					{
						ca5 = new char[ca4.length + nBlanks];
						int i;
						for(i = 0; i < nBlanks; ++i)
							ca5[i] = ' ';
						for(int j = 0; j < ca4.length; ++j)
						{
							ca5[i] = ca4[j];
							++i;
						}
					}
				}
				else if(leadingZeros)
				{
					final int nBlanks = fieldWidth - ca4.length;
					if(nBlanks > 0)
					{
						ca5 = new char[ca4.length + nBlanks];
						int i = 0;
						int j = 0;
						if(ca4[0] == '-')
						{
							ca5[0] = '-';
							++i;
							++j;
						}
						for(int k = 0; k < nBlanks; ++k)
						{
							ca5[i] = '0';
							++i;
						}
						while(j < ca4.length)
						{
							ca5[i] = ca4[j];
							++i;
							++j;
						}
					}
				}
			return ca5;
		}

		private String printFFormat(final double x)
		{
			return fFormatString(x);
		}

		private String printEFormat(final double x)
		{
			if(conversionCharacter == 'e')
				return eFormatString(x, 'e');
			return eFormatString(x, 'E');
		}

		private String printGFormat(final double x)
		{
			final int savePrecision = precision;
			char[] ca4;
			if(Double.isInfinite(x))
			{
				if(x == Double.POSITIVE_INFINITY)
				{
					if(leadingSign)
						ca4 = "+Inf".toCharArray();
					else if(leadingSpace)
						ca4 = " Inf".toCharArray();
					else
						ca4 = "Inf".toCharArray();
				}
				else
					ca4 = "-Inf".toCharArray();
			}
			else if(Double.isNaN(x))
			{
				if(leadingSign)
					ca4 = "+NaN".toCharArray();
				else if(leadingSpace)
					ca4 = " NaN".toCharArray();
				else
					ca4 = "NaN".toCharArray();
			}
			else
			{
				if(!precisionSet)
					precision = 6;
				if(precision == 0)
					precision = 1;
				int ePos = -1;
				String sx;
				if(conversionCharacter == 'g')
				{
					sx = eFormatString(x, 'e').trim();
					ePos = sx.indexOf(101);
				}
				else
				{
					sx = eFormatString(x, 'E').trim();
					ePos = sx.indexOf(69);
				}
				int i = ePos + 1;
				int expon = 0;
				if(sx.charAt(i) == '-')
				{
					++i;
					while(i < sx.length() && sx.charAt(i) == '0')
						++i;
					if(i < sx.length())
						expon = -Integer.parseInt(sx.substring(i));
				}
				else
				{
					if(sx.charAt(i) == '+')
						++i;
					while(i < sx.length() && sx.charAt(i) == '0')
						++i;
					if(i < sx.length())
						expon = Integer.parseInt(sx.substring(i));
				}
				String ret;
				if(!alternateForm)
				{
					String sy;
					if(expon >= -4 && expon < precision)
						sy = fFormatString(x).trim();
					else
						sy = sx.substring(0, ePos);
					for(i = sy.length() - 1; i >= 0 && sy.charAt(i) == '0'; --i)
					{}
					if(i >= 0 && sy.charAt(i) == '.')
						--i;
					String sz;
					if(i == -1)
						sz = "0";
					else if(!Character.isDigit(sy.charAt(i)))
						sz = sy.substring(0, i + 1) + "0";
					else
						sz = sy.substring(0, i + 1);
					if(expon >= -4 && expon < precision)
						ret = sz;
					else
						ret = sz + sx.substring(ePos);
				}
				else if(expon >= -4 && expon < precision)
					ret = fFormatString(x).trim();
				else
					ret = sx;
				if(leadingSpace && x >= 0.0)
					ret = " " + ret;
				ca4 = ret.toCharArray();
			}
			final char[] ca5 = applyFloatPadding(ca4, false);
			precision = savePrecision;
			return new String(ca5);
		}

		private String printDFormat(final short x)
		{
			return this.printDFormat(Short.toString(x));
		}

		private String printDFormat(final long x)
		{
			return this.printDFormat(Long.toString(x));
		}

		private String printDFormat(final int x)
		{
			return this.printDFormat(Integer.toString(x));
		}

		private String printDFormat(String sx)
		{
			int nLeadingZeros = 0;
			int nBlanks = 0;
			int n = 0;
			int i = 0;
			int jFirst = 0;
			final boolean neg = sx.charAt(0) == '-';
			if(sx.equals("0") && precisionSet && precision == 0)
				sx = "";
			if(!neg)
			{
				if(precisionSet && sx.length() < precision)
					nLeadingZeros = precision - sx.length();
			}
			else if(precisionSet && sx.length() - 1 < precision)
				nLeadingZeros = precision - sx.length() + 1;
			if(nLeadingZeros < 0)
				nLeadingZeros = 0;
			if(fieldWidthSet)
			{
				nBlanks = fieldWidth - nLeadingZeros - sx.length();
				if(!neg && (leadingSign || leadingSpace))
					--nBlanks;
			}
			if(nBlanks < 0)
				nBlanks = 0;
			if(leadingSign)
				++n;
			else if(leadingSpace)
				++n;
			n += nBlanks;
			n += nLeadingZeros;
			n += sx.length();
			final char[] ca = new char[n];
			if(leftJustify)
			{
				if(neg)
					ca[i++] = '-';
				else if(leadingSign)
					ca[i++] = '+';
				else if(leadingSpace)
					ca[i++] = ' ';
				final char[] csx = sx.toCharArray();
				jFirst = neg ? 1 : 0;
				for(int j = 0; j < nLeadingZeros; ++j)
				{
					ca[i] = '0';
					++i;
				}
				for(int j = jFirst; j < csx.length; ++j, ++i)
					ca[i] = csx[j];
				for(int j = 0; j < nBlanks; ++j)
				{
					ca[i] = ' ';
					++i;
				}
			}
			else
			{
				if(!leadingZeros)
				{
					for(i = 0; i < nBlanks; ++i)
						ca[i] = ' ';
					if(neg)
						ca[i++] = '-';
					else if(leadingSign)
						ca[i++] = '+';
					else if(leadingSpace)
						ca[i++] = ' ';
				}
				else
				{
					if(neg)
						ca[i++] = '-';
					else if(leadingSign)
						ca[i++] = '+';
					else if(leadingSpace)
						ca[i++] = ' ';
					for(int k = 0; k < nBlanks; ++k, ++i)
						ca[i] = '0';
				}
				for(int k = 0; k < nLeadingZeros; ++k, ++i)
					ca[i] = '0';
				char[] csx;
				int j;
				for(csx = sx.toCharArray(), jFirst = j = neg ? 1 : 0; j < csx.length; ++j, ++i)
					ca[i] = csx[j];
			}
			return new String(ca);
		}

		private String printXFormat(final short x)
		{
			String sx = null;
			if(x == -32768)
				sx = "8000";
			else if(x < 0)
			{
				String t;
				if(x == -32768)
					t = "0";
				else
				{
					t = Integer.toString(~(-x - 1) ^ 0xFFFF8000, 16);
					if(t.charAt(0) == 'F' || t.charAt(0) == 'f')
						t = t.substring(16, 32);
				}

				switch(t.length())
				{
					case 1:
					{
						sx = "800" + t;
						break;
					}
					case 2:
					{
						sx = "80" + t;
						break;
					}
					case 3:
					{
						sx = "8" + t;
						break;
					}
					case 4:
					{
						switch(t.charAt(0))
						{
							case '1':
							{
								sx = "9" + t.substring(1, 4);
								break;
							}
							case '2':
							{
								sx = "a" + t.substring(1, 4);
								break;
							}
							case '3':
							{
								sx = "b" + t.substring(1, 4);
								break;
							}
							case '4':
							{
								sx = "c" + t.substring(1, 4);
								break;
							}
							case '5':
							{
								sx = "d" + t.substring(1, 4);
								break;
							}
							case '6':
							{
								sx = "e" + t.substring(1, 4);
								break;
							}
							case '7':
							{
								sx = "f" + t.substring(1, 4);
								break;
							}
						}
						break;
					}
				}
			}
			else
				sx = Integer.toString(x, 16);
			return this.printXFormat(sx);
		}

		private String printXFormat(final long x)
		{
			String sx = null;
			if(x == Long.MIN_VALUE)
				sx = "8000000000000000";
			else if(x < 0L)
			{
				final String t = Long.toString(~(-x - 1L) ^ Long.MIN_VALUE, 16);
				switch(t.length())
				{
					case 1:
					{
						sx = "800000000000000" + t;
						break;
					}
					case 2:
					{
						sx = "80000000000000" + t;
						break;
					}
					case 3:
					{
						sx = "8000000000000" + t;
						break;
					}
					case 4:
					{
						sx = "800000000000" + t;
						break;
					}
					case 5:
					{
						sx = "80000000000" + t;
						break;
					}
					case 6:
					{
						sx = "8000000000" + t;
						break;
					}
					case 7:
					{
						sx = "800000000" + t;
						break;
					}
					case 8:
					{
						sx = "80000000" + t;
						break;
					}
					case 9:
					{
						sx = "8000000" + t;
						break;
					}
					case 10:
					{
						sx = "800000" + t;
						break;
					}
					case 11:
					{
						sx = "80000" + t;
						break;
					}
					case 12:
					{
						sx = "8000" + t;
						break;
					}
					case 13:
					{
						sx = "800" + t;
						break;
					}
					case 14:
					{
						sx = "80" + t;
						break;
					}
					case 15:
					{
						sx = "8" + t;
						break;
					}
					case 16:
					{
						switch(t.charAt(0))
						{
							case '1':
							{
								sx = "9" + t.substring(1, 16);
								break;
							}
							case '2':
							{
								sx = "a" + t.substring(1, 16);
								break;
							}
							case '3':
							{
								sx = "b" + t.substring(1, 16);
								break;
							}
							case '4':
							{
								sx = "c" + t.substring(1, 16);
								break;
							}
							case '5':
							{
								sx = "d" + t.substring(1, 16);
								break;
							}
							case '6':
							{
								sx = "e" + t.substring(1, 16);
								break;
							}
							case '7':
							{
								sx = "f" + t.substring(1, 16);
								break;
							}
						}
						break;
					}
				}
			}
			else
				sx = Long.toString(x, 16);
			return this.printXFormat(sx);
		}

		private String printXFormat(final int x)
		{
			String sx = null;
			if(x == Integer.MIN_VALUE)
				sx = "80000000";
			else if(x < 0)
			{
				final String t = Integer.toString(~(-x - 1) ^ Integer.MIN_VALUE, 16);
				switch(t.length())
				{
					case 1:
					{
						sx = "8000000" + t;
						break;
					}
					case 2:
					{
						sx = "800000" + t;
						break;
					}
					case 3:
					{
						sx = "80000" + t;
						break;
					}
					case 4:
					{
						sx = "8000" + t;
						break;
					}
					case 5:
					{
						sx = "800" + t;
						break;
					}
					case 6:
					{
						sx = "80" + t;
						break;
					}
					case 7:
					{
						sx = "8" + t;
						break;
					}
					case 8:
					{
						switch(t.charAt(0))
						{
							case '1':
							{
								sx = "9" + t.substring(1, 8);
								break;
							}
							case '2':
							{
								sx = "a" + t.substring(1, 8);
								break;
							}
							case '3':
							{
								sx = "b" + t.substring(1, 8);
								break;
							}
							case '4':
							{
								sx = "c" + t.substring(1, 8);
								break;
							}
							case '5':
							{
								sx = "d" + t.substring(1, 8);
								break;
							}
							case '6':
							{
								sx = "e" + t.substring(1, 8);
								break;
							}
							case '7':
							{
								sx = "f" + t.substring(1, 8);
								break;
							}
						}
						break;
					}
				}
			}
			else
				sx = Integer.toString(x, 16);
			return this.printXFormat(sx);
		}

		private String printXFormat(String sx)
		{
			int nLeadingZeros = 0;
			int nBlanks = 0;
			if(sx.equals("0") && precisionSet && precision == 0)
				sx = "";
			if(precisionSet)
				nLeadingZeros = precision - sx.length();
			if(nLeadingZeros < 0)
				nLeadingZeros = 0;
			if(fieldWidthSet)
			{
				nBlanks = fieldWidth - nLeadingZeros - sx.length();
				if(alternateForm)
					nBlanks -= 2;
			}
			if(nBlanks < 0)
				nBlanks = 0;
			int n = 0;
			if(alternateForm)
				n += 2;
			n += nLeadingZeros;
			n += sx.length();
			n += nBlanks;
			final char[] ca = new char[n];
			int i = 0;
			if(leftJustify)
			{
				if(alternateForm)
				{
					ca[i++] = '0';
					ca[i++] = 'x';
				}
				for(int j = 0; j < nLeadingZeros; ++j, ++i)
					ca[i] = '0';
				final char[] csx = sx.toCharArray();
				for(int k = 0; k < csx.length; ++k, ++i)
					ca[i] = csx[k];
				for(int k = 0; k < nBlanks; ++k, ++i)
					ca[i] = ' ';
			}
			else
			{
				if(!leadingZeros)
					for(int j = 0; j < nBlanks; ++j, ++i)
						ca[i] = ' ';
				if(alternateForm)
				{
					ca[i++] = '0';
					ca[i++] = 'x';
				}
				if(leadingZeros)
					for(int j = 0; j < nBlanks; ++j, ++i)
						ca[i] = '0';
				for(int j = 0; j < nLeadingZeros; ++j, ++i)
					ca[i] = '0';
				final char[] csx = sx.toCharArray();
				for(int k = 0; k < csx.length; ++k, ++i)
					ca[i] = csx[k];
			}
			String caReturn = new String(ca);
			if(conversionCharacter == 'X')
				caReturn = caReturn.toUpperCase();
			return caReturn;
		}

		private String printOFormat(final short x)
		{
			String sx = null;
			if(x == -32768)
				sx = "100000";
			else if(x < 0)
			{
				final String t = Integer.toString(~(-x - 1) ^ 0xFFFF8000, 8);
				switch(t.length())
				{
					case 1:
					{
						sx = "10000" + t;
						break;
					}
					case 2:
					{
						sx = "1000" + t;
						break;
					}
					case 3:
					{
						sx = "100" + t;
						break;
					}
					case 4:
					{
						sx = "10" + t;
						break;
					}
					case 5:
					{
						sx = "1" + t;
						break;
					}
				}
			}
			else
				sx = Integer.toString(x, 8);
			return this.printOFormat(sx);
		}

		private String printOFormat(final long x)
		{
			String sx = null;
			if(x == Long.MIN_VALUE)
				sx = "1000000000000000000000";
			else if(x < 0L)
			{
				final String t = Long.toString(~(-x - 1L) ^ Long.MIN_VALUE, 8);
				switch(t.length())
				{
					case 1:
					{
						sx = "100000000000000000000" + t;
						break;
					}
					case 2:
					{
						sx = "10000000000000000000" + t;
						break;
					}
					case 3:
					{
						sx = "1000000000000000000" + t;
						break;
					}
					case 4:
					{
						sx = "100000000000000000" + t;
						break;
					}
					case 5:
					{
						sx = "10000000000000000" + t;
						break;
					}
					case 6:
					{
						sx = "1000000000000000" + t;
						break;
					}
					case 7:
					{
						sx = "100000000000000" + t;
						break;
					}
					case 8:
					{
						sx = "10000000000000" + t;
						break;
					}
					case 9:
					{
						sx = "1000000000000" + t;
						break;
					}
					case 10:
					{
						sx = "100000000000" + t;
						break;
					}
					case 11:
					{
						sx = "10000000000" + t;
						break;
					}
					case 12:
					{
						sx = "1000000000" + t;
						break;
					}
					case 13:
					{
						sx = "100000000" + t;
						break;
					}
					case 14:
					{
						sx = "10000000" + t;
						break;
					}
					case 15:
					{
						sx = "1000000" + t;
						break;
					}
					case 16:
					{
						sx = "100000" + t;
						break;
					}
					case 17:
					{
						sx = "10000" + t;
						break;
					}
					case 18:
					{
						sx = "1000" + t;
						break;
					}
					case 19:
					{
						sx = "100" + t;
						break;
					}
					case 20:
					{
						sx = "10" + t;
						break;
					}
					case 21:
					{
						sx = "1" + t;
						break;
					}
				}
			}
			else
				sx = Long.toString(x, 8);
			return this.printOFormat(sx);
		}

		private String printOFormat(final int x)
		{
			String sx = null;
			if(x == Integer.MIN_VALUE)
				sx = "20000000000";
			else if(x < 0)
			{
				final String t = Integer.toString(~(-x - 1) ^ Integer.MIN_VALUE, 8);
				switch(t.length())
				{
					case 1:
					{
						sx = "2000000000" + t;
						break;
					}
					case 2:
					{
						sx = "200000000" + t;
						break;
					}
					case 3:
					{
						sx = "20000000" + t;
						break;
					}
					case 4:
					{
						sx = "2000000" + t;
						break;
					}
					case 5:
					{
						sx = "200000" + t;
						break;
					}
					case 6:
					{
						sx = "20000" + t;
						break;
					}
					case 7:
					{
						sx = "2000" + t;
						break;
					}
					case 8:
					{
						sx = "200" + t;
						break;
					}
					case 9:
					{
						sx = "20" + t;
						break;
					}
					case 10:
					{
						sx = "2" + t;
						break;
					}
					case 11:
					{
						sx = "3" + t.substring(1);
						break;
					}
				}
			}
			else
				sx = Integer.toString(x, 8);
			return this.printOFormat(sx);
		}

		private String printOFormat(String sx)
		{
			int nLeadingZeros = 0;
			int nBlanks = 0;
			if(sx.equals("0") && precisionSet && precision == 0)
				sx = "";
			if(precisionSet)
				nLeadingZeros = precision - sx.length();
			if(alternateForm)
				++nLeadingZeros;
			if(nLeadingZeros < 0)
				nLeadingZeros = 0;
			if(fieldWidthSet)
				nBlanks = fieldWidth - nLeadingZeros - sx.length();
			if(nBlanks < 0)
				nBlanks = 0;
			final int n = nLeadingZeros + sx.length() + nBlanks;
			final char[] ca = new char[n];
			if(leftJustify)
			{
				int i;
				for(i = 0; i < nLeadingZeros; ++i)
					ca[i] = '0';
				final char[] csx = sx.toCharArray();
				for(int j = 0; j < csx.length; ++j, ++i)
					ca[i] = csx[j];
				for(int j = 0; j < nBlanks; ++j, ++i)
					ca[i] = ' ';
			}
			else
			{
				int i;
				if(leadingZeros)
					for(i = 0; i < nBlanks; ++i)
						ca[i] = '0';
				else
					for(i = 0; i < nBlanks; ++i)
						ca[i] = ' ';
				for(int k = 0; k < nLeadingZeros; ++k, ++i)
					ca[i] = '0';
				final char[] csx = sx.toCharArray();
				for(int j = 0; j < csx.length; ++j, ++i)
					ca[i] = csx[j];
			}
			return new String(ca);
		}

		private String printCFormat(final char x)
		{
			final int nPrint = 1;
			int width = fieldWidth;
			if(!fieldWidthSet)
				width = nPrint;
			final char[] ca = new char[width];
			int i = 0;
			if(leftJustify)
			{
				ca[0] = x;
				for(i = 1; i <= width - nPrint; ++i)
					ca[i] = ' ';
			}
			else
			{
				for(i = 0; i < width - nPrint; ++i)
					ca[i] = ' ';
				ca[i] = x;
			}
			return new String(ca);
		}

		private String printSFormat(final String x)
		{
			int nPrint = x.length();
			int width = fieldWidth;
			if(precisionSet && nPrint > precision)
				nPrint = precision;
			if(!fieldWidthSet)
				width = nPrint;
			int n = 0;
			if(width > nPrint)
				n += width - nPrint;
			if(nPrint >= x.length())
				n += x.length();
			else
				n += nPrint;
			final char[] ca = new char[n];
			int i = 0;
			if(leftJustify)
			{
				if(nPrint >= x.length())
				{
					final char[] csx = x.toCharArray();
					for(i = 0; i < x.length(); ++i)
						ca[i] = csx[i];
				}
				else
				{
					final char[] csx = x.substring(0, nPrint).toCharArray();
					for(i = 0; i < nPrint; ++i)
						ca[i] = csx[i];
				}
				for(int j = 0; j < width - nPrint; ++j, ++i)
					ca[i] = ' ';
			}
			else
			{
				for(i = 0; i < width - nPrint; ++i)
					ca[i] = ' ';
				if(nPrint >= x.length())
				{
					final char[] csx = x.toCharArray();
					for(int k = 0; k < x.length(); ++k)
					{
						ca[i] = csx[k];
						++i;
					}
				}
				else
				{
					final char[] csx = x.substring(0, nPrint).toCharArray();
					for(int k = 0; k < nPrint; ++k)
					{
						ca[i] = csx[k];
						++i;
					}
				}
			}
			return new String(ca);
		}

		private boolean setConversionCharacter()
		{
			boolean ret = false;
			conversionCharacter = '\0';
			if(pos < fmt.length())
			{
				final char c = fmt.charAt(pos);
				if(c == 'i' || c == 'd' || c == 'f' || c == 'g' || c == 'G' || c == 'o' || c == 'x' || c == 'X' || c == 'e' || c == 'E' || c == 'c' || c == 's' || c == '%')
				{
					conversionCharacter = c;
					++pos;
					ret = true;
				}
			}
			return ret;
		}

		private void setOptionalHL()
		{
			optionalh = false;
			optionall = false;
			optionalL = false;
			if(pos < fmt.length())
			{
				final char c = fmt.charAt(pos);
				if(c == 'h')
				{
					optionalh = true;
					++pos;
				}
				else if(c == 'l')
				{
					optionall = true;
					++pos;
				}
				else if(c == 'L')
				{
					optionalL = true;
					++pos;
				}
			}
		}

		private void setPrecision()
		{
			final int firstPos = pos;
			precisionSet = false;
			if(pos < fmt.length() && fmt.charAt(pos) == '.')
			{
				++pos;
				if(pos < fmt.length() && fmt.charAt(pos) == '*')
				{
					++pos;
					if(!setPrecisionArgPosition())
					{
						variablePrecision = true;
						precisionSet = true;
					}
					return;
				}
				while(pos < fmt.length())
				{
					final char c = fmt.charAt(pos);
					if(!Character.isDigit(c))
						break;
					++pos;
				}
				if(pos > firstPos + 1)
				{
					final String sz = fmt.substring(firstPos + 1, pos);
					precision = Integer.parseInt(sz);
					precisionSet = true;
				}
			}
		}

		private void setFieldWidth()
		{
			final int firstPos = pos;
			fieldWidth = 0;
			fieldWidthSet = false;
			if(pos < fmt.length() && fmt.charAt(pos) == '*')
			{
				++pos;
				if(!setFieldWidthArgPosition())
				{
					variableFieldWidth = true;
					fieldWidthSet = true;
				}
			}
			else
			{
				while(pos < fmt.length())
				{
					final char c = fmt.charAt(pos);
					if(!Character.isDigit(c))
						break;
					++pos;
				}
				if(firstPos < pos && firstPos < fmt.length())
				{
					final String sz = fmt.substring(firstPos, pos);
					fieldWidth = Integer.parseInt(sz);
					fieldWidthSet = true;
				}
			}
		}

		private void setArgPosition()
		{
			int xPos;
			for(xPos = pos; xPos < fmt.length() && Character.isDigit(fmt.charAt(xPos)); ++xPos)
			{}
			if(xPos > pos && xPos < fmt.length() && fmt.charAt(xPos) == '$')
			{
				positionalSpecification = true;
				argumentPosition = Integer.parseInt(fmt.substring(pos, xPos));
				pos = xPos + 1;
			}
		}

		private boolean setFieldWidthArgPosition()
		{
			boolean ret = false;
			int xPos;
			for(xPos = pos; xPos < fmt.length() && Character.isDigit(fmt.charAt(xPos)); ++xPos)
			{}
			if(xPos > pos && xPos < fmt.length() && fmt.charAt(xPos) == '$')
			{
				positionalFieldWidth = true;
				argumentPositionForFieldWidth = Integer.parseInt(fmt.substring(pos, xPos));
				pos = xPos + 1;
				ret = true;
			}
			return ret;
		}

		private boolean setPrecisionArgPosition()
		{
			boolean ret = false;
			int xPos;
			for(xPos = pos; xPos < fmt.length() && Character.isDigit(fmt.charAt(xPos)); ++xPos)
			{}
			if(xPos > pos && xPos < fmt.length() && fmt.charAt(xPos) == '$')
			{
				positionalPrecision = true;
				argumentPositionForPrecision = Integer.parseInt(fmt.substring(pos, xPos));
				pos = xPos + 1;
				ret = true;
			}
			return ret;
		}

		boolean isPositionalSpecification()
		{
			return positionalSpecification;
		}

		int getArgumentPosition()
		{
			return argumentPosition;
		}

		boolean isPositionalFieldWidth()
		{
			return positionalFieldWidth;
		}

		int getArgumentPositionForFieldWidth()
		{
			return argumentPositionForFieldWidth;
		}

		boolean isPositionalPrecision()
		{
			return positionalPrecision;
		}

		int getArgumentPositionForPrecision()
		{
			return argumentPositionForPrecision;
		}

		private void setFlagCharacters()
		{
			thousands = false;
			leftJustify = false;
			leadingSign = false;
			leadingSpace = false;
			alternateForm = false;
			leadingZeros = false;
			while(pos < fmt.length())
			{
				final char c = fmt.charAt(pos);
				if(c == '\'')
					thousands = true;
				else if(c == '-')
				{
					leftJustify = true;
					leadingZeros = false;
				}
				else if(c == '+')
				{
					leadingSign = true;
					leadingSpace = false;
				}
				else if(c == ' ')
				{
					if(!leadingSign)
						leadingSpace = true;
				}
				else if(c == '#')
					alternateForm = true;
				else
				{
					if(c != '0')
						break;
					if(!leftJustify)
						leadingZeros = true;
				}
				++pos;
			}
		}
	}
}
