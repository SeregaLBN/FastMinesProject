package fmg.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public class PrintStreamWrapper extends PrintStream {

	public PrintStreamWrapper(File file) throws FileNotFoundException { super(file); }
	public PrintStreamWrapper(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException { super(file, csn); }
	public PrintStreamWrapper(OutputStream out) { super(out); }
	public PrintStreamWrapper(OutputStream out, boolean autoFlush) { super(out, autoFlush); }
    public PrintStreamWrapper(OutputStream out, boolean autoFlush, String encoding) throws UnsupportedEncodingException { super(out, autoFlush, encoding); }
    public PrintStreamWrapper(String fileName) throws FileNotFoundException { super(fileName); }
   	public PrintStreamWrapper(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException { super(fileName, csn); }

	@Override
	public PrintStream append(char c) { return super.append(c); }
	@Override
	public PrintStream append(CharSequence csq) { return super.append(csq); }
	@Override
	public PrintStream append(CharSequence csq, int start, int end) { return super.append(csq, start, end); }
	@Override
	public boolean checkError() { return super.checkError(); }
	@Override
	protected void clearError() { super.clearError(); }
	@Override
	protected Object clone() throws CloneNotSupportedException { return super.clone(); }
	@Override
	public void close() { super.close(); }
	@Override
	public boolean equals(Object obj) { return super.equals(obj); }
	@Override
	protected void finalize() throws Throwable { super.finalize(); }
	@Override
	public void flush() { super.flush(); }
	@Override
	public PrintStream format(Locale l, String format, Object... args) { return super.format(l, format, args); }
	@Override
	public PrintStream format(String format, Object... args) { return super.format(format, args); }
	@Override
	public int hashCode() { return super.hashCode(); }
	@Override
	public void print(boolean b) { super.print(b); }
	@Override
	public void print(char c) { super.print(c); }
	@Override
	public void print(char[] s) { super.print(s); }
	@Override
	public void print(double d) { super.print(d); }
	@Override
	public void print(float f) { super.print(f); }
	@Override
	public void print(int i) { super.print(i); }
	@Override
	public void print(long l) { super.print(l); }
	@Override
	public void print(Object obj) { super.print(obj); }
	@Override
	public void print(String s) { super.print(s); }
	@Override
	public PrintStream printf(Locale l, String format, Object... args) { return super.printf(l, format, args); }
	@Override
	public PrintStream printf(String format, Object... args) { return super.printf(format, args); }
	@Override
	public void println() { super.println(); }
	@Override
	public void println(boolean x) { super.println(x); }
	@Override
	public void println(char x) { super.println(x); }
	@Override
	public void println(char[] x) { super.println(x); }
	@Override
	public void println(double x) { super.println(x); }
	@Override
	public void println(float x) { super.println(x); }
	@Override
	public void println(int x) { super.println(x); }
	@Override
	public void println(long x) { super.println(x); }
	@Override
	public void println(Object x) { super.println(x); }
	@Override
	public void println(String x) { super.println(x); }
	@Override
	protected void setError() { super.setError(); }
	@Override
	public String toString() { return super.toString(); }
	@Override
	public void write(byte[] b) throws IOException { super.write(b); }
	@Override
	public void write(byte[] buf, int off, int len) { super.write(buf, off, len); }
	@Override
	public void write(int b) { super.write(b); }
}
