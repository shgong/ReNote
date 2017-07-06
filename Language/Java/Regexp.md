# The String Story

How to check rules and validation? It would be too long using conditional statement, regexp will have more concise code.

## Common Uses
1.Validation
Phone Numbers 
Email
Password
Domain Names

2.Searching
Word in a sentence
Unwanted Characters
Extracting sections
Replacing/Cleaning/Formatting

## Symbols

|   Symbol    | Expression       |
| ----------- |----------------- |
|  &#124;     |      or          |
|  //         |  pattern         |
|  ()         |  group        |
|  +          |  repeat          |
|  ^          |  Not             |
|  *          |  {0,}             |
|  ?          |  optional        |
|  []         | character set    |
|  {}         | interval times (at least, at most)    |
|  ^          | start string       |
|  $          | end string    |

group will return values
(?:street|lane)   non-capture group, not return value

# Meta Character
|   Symbol     | Expression       |
| -----------  |----------------- |
|  \s          |  whitespaces     |
|  \w+         |  word-like character  |
|  .           | any character except newline |
|  \b           | word boundry |
|  \d           | number |

negated shorthand: [^\d] == \D, same to \W \S
tip: use backslash to escape special character, `\+`, `\.`

## Modifier
letter after final slash

e.g.
i - case insensitive
g - global, as many time as possible
m - multiline, match ^$ to line start/end

# Example

```js
// phone number example
if (chars.match(/regular expression/))

email:  /^\w+@\w+\.(com|net|org|edu)$/i
twiiter: /http:\/\/www\.twitter\.com\/\w+/
currency: /\$[0-9]+\.[0-9][0-9]/

singleword: /\b\w+\b/g
ok Okay: /\b(ok(ay)?|sure|y(es)?)\b/i
non vowel: /\b[^aeiouy\s]+\b/
```
