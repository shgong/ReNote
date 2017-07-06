# Bash Lecture

## variable
```sh
#!/bin/bash
# create user defined variables
declare -i number1
declare -i number2
declare -i total

echo "enter first number"
read number1
echo "enter second number"
read number2

# total
total=$number*$number2
echo "total = " $total
```

- php also use echo function to print
- $: get value of variable => string interpolation

## for loop

```sh
#!/bin/bash
for i in {0..10..2}
do
echo "Value is " $i
done
```

- for loop: start, end, increment
- start and end are inclusive

## if fi
```sh
if [ "$color1" == "$color2" ]
then 
echo "Equal color"
else
echo "Different color"
fi
exit 0
```

## while & compare

```sh
declare -i counter
counter = 10
while [ $counter -gt 2 ]; do
echo "The counter is " $counter
counter = counter - 1
done
exit0
```

## case  esac
```sh
echo "Enter car model"
read car

case $car in
    bmw | maserati ) echo 'Rank 1';;
    Honda | Nissan ) echo 'Rank 2';;
    Toyota | mazda ) echo 'Rank 3';;
    * ) echo 'No ranking found'
esac
exit 0
```

### Playground
```sh
read k  #123
echo $[$k - 100] #23
echo $(( k > 100)) #1

if [ 100 -gt 99 ]; then echo "ss"; fi  # ss

```

