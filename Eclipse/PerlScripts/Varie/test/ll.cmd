awk -F: '{ printf "%5.5s\n",  }' selRS.ord  | uniq -d -c > selRS5.txt
