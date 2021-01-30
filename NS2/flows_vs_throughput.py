import matplotlib.pyplot as plt 
  
# x axis values 
x = [10,20,30,40,50] 
# corresponding y axis values 
y = [75591.8,141453,156359,112996,85436.7] 
  

# plotting the points  
plt.plot(x, y, color='violet', linewidth = 2, 
         marker='o', markerfacecolor='red', markersize=8) 
  
#fig, ax=plt.subplots()
# setting x and y axis range 
#plt.plot(range(5));
plt.ylim(50000,200000) 
#plt.xlim(100,1500) 

#plt.yticks(y)
plt.xticks(x)

#ax.plot(x,y)
  
# naming the x axis 
plt.xlabel('X axis: No. of Flows') 
# naming the y axis 
plt.ylabel('Y axis: Throughput') 
  
# giving a title to my graph 
plt.title('No. of Flows vs Throughput') 
  
plt.grid()
# function to show the plot 
plt.show()
