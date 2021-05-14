find . -type f -name 'Bader*' | while read FILE ; do
    newfile="$(echo ${FILE} |sed -e 's/slashdot0811/com-amazon/')" ;
    mv "${FILE}" "${newfile}" ;
done

find . -type f -name 'Bader*' | while read FILE ; do
    newfile="$(echo ${FILE} |sed -e 's/as-caida20071105/slashdot0811/')" ;
    mv "${FILE}" "${newfile}" ;
done 
