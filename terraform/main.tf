terraform {
  required_version = ">= 1.5.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.0.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

resource "aws_vpc" "buildnest" {
  cidr_block           = var.vpc_cidr
  enable_dns_support   = true
  enable_dns_hostnames = true
  tags = {
    Name = "buildnest-vpc"
  }
}

resource "aws_subnet" "public" {
  vpc_id                  = aws_vpc.buildnest.id
  cidr_block              = var.public_subnet_cidr
  map_public_ip_on_launch = true
  availability_zone       = var.aws_az
  tags = {
    Name = "buildnest-public-subnet"
  }
}

resource "aws_subnet" "public_2" {
  vpc_id                  = aws_vpc.buildnest.id
  cidr_block              = var.public_subnet_cidr_2
  map_public_ip_on_launch = true
  availability_zone       = var.aws_az_2
  tags = {
    Name = "buildnest-public-subnet-2"
  }
}

resource "aws_subnet" "private_1" {
  vpc_id            = aws_vpc.buildnest.id
  cidr_block        = var.private_subnet_cidr_1
  availability_zone = var.aws_az
  tags = {
    Name = "buildnest-private-subnet-1"
  }
}

resource "aws_subnet" "private_2" {
  vpc_id            = aws_vpc.buildnest.id
  cidr_block        = var.private_subnet_cidr_2
  availability_zone = var.aws_az_2
  tags = {
    Name = "buildnest-private-subnet-2"
  }
}

resource "aws_internet_gateway" "buildnest" {
  vpc_id = aws_vpc.buildnest.id
  tags = {
    Name = "buildnest-igw"
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.buildnest.id
  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.buildnest.id
  }
  tags = {
    Name = "buildnest-public-rt"
  }
}

resource "aws_route_table_association" "public" {
  subnet_id      = aws_subnet.public.id
  route_table_id = aws_route_table.public.id
}

resource "aws_route_table_association" "public_2" {
  subnet_id      = aws_subnet.public_2.id
  route_table_id = aws_route_table.public.id
}

resource "aws_security_group" "app_sg" {
  name        = "buildnest-app-sg"
  description = "Allow HTTP/HTTPS"
  vpc_id      = aws_vpc.buildnest.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port       = 8080
    to_port         = 8080
    protocol        = "tcp"
    security_groups = [aws_security_group.alb_sg.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "buildnest-app-sg"
  }
}
