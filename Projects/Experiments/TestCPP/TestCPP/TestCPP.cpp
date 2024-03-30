// TestCPP.cpp : Defines the entry point for the console application.
//

#include "stdafx.h"


int _tmain(int argc, _TCHAR* argv[])
{
	//I can declare a string see here
	std::string mystr = "Hello World";
	std::cout << "Salve Mondo" << std::endl;    //Error no operator '<<' matches these operands
	return 0;
}

