// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codeu.mathlang.impl;

import java.io.IOException;

import com.google.codeu.mathlang.core.tokens.NameToken;
import com.google.codeu.mathlang.core.tokens.NumberToken;
import com.google.codeu.mathlang.core.tokens.StringToken;
import com.google.codeu.mathlang.core.tokens.SymbolToken;
import com.google.codeu.mathlang.core.tokens.Token;
import com.google.codeu.mathlang.parsing.TokenReader;

// MY TOKEN READER
//
// This is YOUR implementation of the token reader interface. To know how
// it should work, read src/com/google/codeu/mathlang/parsing/TokenReader.java.
// You should not need to change any other files to get your token reader to
// work with the test of the system.
public final class MyTokenReader implements TokenReader {

	/**
	 * String that will be tokenized. Passed Through constructor
	 */
	private String source;
	/**
	 * The index that the tokenizer is currently at. When nextArg() is called, the
	 * argument after the current index will be returned
	 */
	private int currentIndex = 0;
	/**
	 * All symbols that cannot appear in names (if one appears, it will be counted
	 * as a separate symbol Token, not as a part of the name)
	 */
	private char[] symbols = { '=', '+', '-', ';' };

	public MyTokenReader(String source) {
		this.source = source;
		// Your token reader will only be given a string for input. The string will
		// contain the whole source (0 or more lines).
	}

	/**
	 * Returns the argument directly after the currentIndex
	 * 
	 * @return the next argument in the source string
	 */
	@Override
	public Token next() throws IOException {
		// Most of your work will take place here. For every call to |next| you should
		// return a token until you reach the end. When there are no more tokens, you
		// should return |null| to signal the end of input.

		// If for any reason you detect an error in the input, you may throw an
		// IOException
		// which will stop all execution.
		if (!this.hasNextArg()) {
			return null;
		}
		// skip all spaces
		while (source.charAt(currentIndex) == ' ') {
			currentIndex++;
		}
		String nextString;
		boolean wasWrappedinQuotes = false;
		// if quotes is first, return string between the two quotes
		if (source.charAt(currentIndex) == '"') {
			currentIndex++;
			wasWrappedinQuotes = true;
			nextString = nextStringUntil('"');
		} else {
			nextString = nextStringUntil(' ');
		}

		// if length is 0, aka if string is '\n' or '\t' which have length 0, then call
		// next again ignoring the escape character
		if (nextString.length() == 0) {
			return next();
		}

		if (isNumber(nextString)) {
			return new NumberToken(Double.parseDouble(nextString));
		}

		// if the first character is a letter and does not contains spaces, return a
		// name token.
		if (Character.isLetter(nextString.charAt(0)) && !wasWrappedinQuotes) {
			return new NameToken(nextString);
		}

		// Parse the nextString into a Token
		// if it is a symbol (1 character long), return symbol token
		if (nextString.length() == 1) {
			return new SymbolToken(nextString.charAt(0));
		}

		return new StringToken(nextString);
	}

	/**
	 * 
	 * @return If there are any args the tokenizer has not yet returned
	 */
	public boolean hasNextArg() {
		int remainingIndices = source.length() - currentIndex;
		return remainingIndices > 0;
	}

	// returns a String starting at currentIndex until the next occurrence of
	// endingChar.
	// Does not include endingChar in the String
	// Leaves currentIndex one character after the end of the returned string.
	private String nextStringUntil(char endingChar) {
		String toReturn = "";
		while (hasNextArg() && source.charAt(currentIndex) != endingChar && source.charAt(currentIndex) != '\n') {
			char nextChar = source.charAt(currentIndex);
			// if ending char is not a quote, check for symbols
			// if ending char is a quote, then this loop is parsing a string, in which case
			// we don't want to disregard symbols apart of the strings
			if (endingChar != '"') {
				// if next char is a symbol and some other value already populates toReturn,
				// return toReturn
				// this happens when a name is being read, but then a symbol appears before the
				// space does
				if (isSymbol(nextChar) && !toReturn.isEmpty()) {
					return toReturn;
				} else if (isSymbol(nextChar) && toReturn.isEmpty()) {
					toReturn += nextChar;
					break;
				}
			}
			toReturn += nextChar;
			currentIndex++;
		}
		currentIndex++;
		return toReturn;
	}

	// Helper string methods
	/**
	 * Returns true if a string only contains digits and 1 '.' returns false
	 * otherwise
	 * 
	 * @param name
	 *            the string being tested
	 * @return
	 */
	private boolean isNumber(String name) {

		char[] chars = name.toCharArray();
		boolean isDecimalRecorded = false;
		int index = 0;
		for (char c : chars) {
			// will determine that string is not a number IF:
			// c is not a digit
			// AND
			// c is not the first decimal
			// AND
			// c is not the - sign at index 0 (and also the string is not just a negative
			// sign)
			if (!(Character.isDigit(c)) && !(c == '.' && !isDecimalRecorded)
					&& !(c == '-' && index == 0 && chars.length > 1)) {
				return false;
			}
			// if c is a period, change isDecimalRecorded to true to ensure that two
			// decimals can't exist in a string
			if (c == '.') {
				isDecimalRecorded = true;
			}
			index++;
		}
		// return false if chars length is 0
		return chars.length != 0;
	}

	private boolean isSymbol(char c) {
		for (char symbol : this.symbols) {
			if (c == symbol) {
				return true;
			}
		}
		return false;
	}
}
